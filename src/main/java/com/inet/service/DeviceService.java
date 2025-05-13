package com.inet.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.inet.entity.Device;
import com.inet.entity.School;
import com.inet.repository.DeviceRepository;
import com.inet.repository.SchoolRepository;
import com.inet.entity.Classroom;
import com.inet.repository.ClassroomRepository;
import com.inet.entity.Manage;
import com.inet.repository.ManageRepository;
import com.inet.entity.Operator;
import com.inet.service.OperatorService;
import com.inet.service.ClassroomService;
import com.inet.entity.Uid;
import com.inet.service.UidService;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    private final SchoolRepository schoolRepository;
    private final ClassroomRepository classroomRepository;
    private final OperatorService operatorService;
    private final ManageRepository manageRepository;
    private final ClassroomService classroomService;
    private final UidService uidService;
    
    // Create
    public Device saveDevice(Device device) {
        log.info("Saving device: {}", device);
        return deviceRepository.save(device);
    }
    
    // Read
    public List<Device> getAllDevices() {
        log.info("Getting all devices");
        return deviceRepository.findAll();
    }
    
    public List<Device> getDevicesBySchoolId(Long schoolId) {
        log.info("Getting devices by school id: {}", schoolId);
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
        return deviceRepository.findBySchool(school);
    }
    
    public Optional<Device> getDeviceById(Long id) {
        log.info("Getting device by id: {}", id);
        return deviceRepository.findById(id);
    }
    
    // Update
    public Device updateDevice(Device device) {
        log.info("Updating device: {}", device);
        return deviceRepository.save(device);
    }
    
    // Delete
    @Transactional
    public void deleteDevice(Long id) {
        log.info("Deleting device with id: {}", id);
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));
        
        // 장비의 운영자 참조를 null로 설정
        device.setOperator(null);
        deviceRepository.save(device);
        
        // 장비 삭제
        deviceRepository.deleteById(id);
    }

    // 페이징 + 학교 + 타입 + 교실 조건 검색
    public Page<Device> getDevices(Long schoolId, String type, Long classroomId, Pageable pageable) {
        if (schoolId != null && type != null && !type.isEmpty() && classroomId != null) {
            School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
            Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + classroomId));
            return deviceRepository.findBySchoolAndTypeAndClassroom(school, type, classroom, pageable);
        } else if (schoolId != null && type != null && !type.isEmpty()) {
            School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
            return deviceRepository.findBySchoolAndType(school, type, pageable);
        } else if (schoolId != null && classroomId != null) {
            School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
            Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + classroomId));
            return deviceRepository.findBySchoolAndClassroom(school, classroom, pageable);
        } else if (schoolId != null) {
            School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
            return deviceRepository.findBySchool(school, pageable);
        } else if (type != null && !type.isEmpty()) {
            return deviceRepository.findByType(type, pageable);
        } else if (classroomId != null) {
            Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + classroomId));
            return deviceRepository.findByClassroom(classroom, pageable);
        } else {
            return deviceRepository.findAll(pageable);
        }
    }

    // type 목록 조회
    public List<String> getAllTypes() {
        return deviceRepository.findDistinctTypes();
    }

    public List<Device> findBySchool(Long schoolId) {
        return deviceRepository.findBySchoolSchoolId(schoolId);
    }

    public List<Device> findByType(String type) {
        return deviceRepository.findByType(type);
    }

    public List<Device> findBySchoolAndType(Long schoolId, String type) {
        return deviceRepository.findBySchoolSchoolIdAndType(schoolId, type);
    }

    public List<Device> findAll() {
        return deviceRepository.findAll();
    }

    public void exportToExcel(List<Device> devices, OutputStream outputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("장비 목록");
        
        // 1. 제목 스타일 (학교명 + 교실배치별 장비현황)
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        
        // 2. 날짜 스타일
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setAlignment(HorizontalAlignment.RIGHT);
        Font dateFont = workbook.createFont();
        dateFont.setBold(true);
        dateStyle.setFont(dateFont);
        
        // 3. 헤더 스타일
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        
        // 4. 데이터 스타일
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        
        // 학교명 가져오기 - 첫 번째 장비의 학교명 사용 (또는 선택된 학교명)
        String schoolName = "학교";
        if (!devices.isEmpty() && devices.get(0).getSchool() != null && devices.get(0).getSchool().getSchoolName() != null) {
            schoolName = devices.get(0).getSchool().getSchoolName();
        }
        
        // 현재 날짜 (작성일자)
        String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // 첫번째 행: 제목 (학교명 + 교실배치별 장비현황)
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(schoolName + " 교실배치별 장비현황");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13)); // A1:N1 (비고 컬럼까지 병합)
        
        // 두번째 행: 작성일자
        Row dateRow = sheet.createRow(1);
        Cell dateCell = dateRow.createCell(12); // M2 셀
        dateCell.setCellValue("작성일자");
        dateCell.setCellStyle(dateStyle);
        
        Cell todayCell = dateRow.createCell(13); // N2 셀
        todayCell.setCellValue(today);
        todayCell.setCellStyle(dateStyle);
        
        // 세번째 행: 헤더
        Row headerRow = sheet.createRow(2);
        String[] headers = {"No", "고유번호", "관리번호", "종류", "직위", "취급자", "제조사", "모델명", "도입일자", "현IP주소", "설치장소", "용도", "세트분류", "비고"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 데이터 행 추가
        int rowNum = 3; // 4번째 행부터 데이터 시작
        
        // 교실, 세트 타입, 담당자 순으로 정렬된 데이터 추가
        for (int i = 0; i < devices.size(); i++) {
            Device device = devices.get(i);
            Row row = sheet.createRow(rowNum++);
            
            // 각 열에 데이터 추가 및 스타일 적용
            for (int col = 0; col < 14; col++) {
                Cell cell = row.createCell(col);
                cell.setCellStyle(dataStyle);
                
                // 각 열의 데이터 설정
                switch (col) {
                    case 0: // No (순번)
                        cell.setCellValue(i + 1);
                        break;
                    case 1: // 고유번호
                        cell.setCellValue(device.getUid() != null ? device.getUid().getCate() + device.getUid().getIdNumber() : "");
                        break;
                    case 2: // 관리번호
                        String manageNo = "";
                        Manage manage = device.getManage();
                        if (manage != null) {
                            String cate = manage.getManageCate() != null ? manage.getManageCate() : "";
                            String year = manage.getYear() != null ? manage.getYear().toString() : "";
                            String num = manage.getManageNum() != null ? manage.getManageNum().toString() : "";
                            manageNo = (cate + (year.isEmpty() ? "" : ("-" + year)) + (num.isEmpty() ? "" : ("-" + num))).replaceAll("-$", "");
                        }
                        cell.setCellValue(manageNo);
                        break;
                    case 3: // 종류
                        cell.setCellValue(device.getType() != null ? device.getType() : "");
                        break;
                    case 4: // 직위
                        cell.setCellValue(device.getOperator() != null && device.getOperator().getPosition() != null ? device.getOperator().getPosition() : "");
                        break;
                    case 5: // 취급자
                        cell.setCellValue(device.getOperator() != null && device.getOperator().getName() != null ? device.getOperator().getName() : "");
                        break;
                    case 6: // 제조사
                        cell.setCellValue(device.getManufacturer() != null ? device.getManufacturer() : "");
                        break;
                    case 7: // 모델명
                        cell.setCellValue(device.getModelName() != null ? device.getModelName() : "");
                        break;
                    case 8: // 도입일자
                        cell.setCellValue(device.getPurchaseDate() != null ? device.getPurchaseDate().toString() : "");
                        break;
                    case 9: // 현IP주소
                        cell.setCellValue(device.getIpAddress() != null ? device.getIpAddress() : "");
                        break;
                    case 10: // 설치장소
                        cell.setCellValue(device.getClassroom() != null && device.getClassroom().getRoomName() != null ? device.getClassroom().getRoomName() : "");
                        break;
                    case 11: // 용도
                        cell.setCellValue(device.getPurpose() != null ? device.getPurpose() : "");
                        break;
                    case 12: // 세트분류
                        cell.setCellValue(device.getSetType() != null ? device.getSetType() : "");
                        break;
                    case 13: // 비고
                        cell.setCellValue(device.getNote() != null ? device.getNote() : "");
                        break;
                }
            }
        }

        // 컬럼 너비 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            // 최소 너비 보장
            int currentWidth = sheet.getColumnWidth(i);
            if (currentWidth < 3000) {
                sheet.setColumnWidth(i, 3000);
            }
        }

        // 첫 번째 컬럼(No)은 좁게 설정
        sheet.setColumnWidth(0, 1500);
        
        // 비고 컬럼(13번째)에 자동 줄바꿈 설정
        CellStyle wrappingStyle = workbook.createCellStyle();
        wrappingStyle.cloneStyleFrom(dataStyle);
        wrappingStyle.setWrapText(true);
        
        // 비고 컬럼에 줄바꿈 스타일 적용
        for (int i = 3; i < rowNum; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell noteCell = row.getCell(13);
                if (noteCell != null) {
                    // 기존 텍스트 가져오기
                    String noteText = noteCell.getStringCellValue();
                    // 15자마다 줄바꿈 처리
                    if (noteText != null && noteText.length() > 15) {
                        noteCell.setCellStyle(wrappingStyle);
                    }
                }
            }
        }

        workbook.write(outputStream);
        workbook.close();
    }

    public List<Device> findByClassroomRoomName(String roomName) {
        return deviceRepository.findByClassroomRoomName(roomName);
    }

    public List<Device> findBySchoolId(Long schoolId) {
        return deviceRepository.findBySchoolSchoolId(schoolId);
    }

    public List<Device> findBySchoolAndTypeAndClassroom(Long schoolId, String type, Long classroomId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + classroomId));
        return deviceRepository.findBySchoolAndTypeAndClassroom(school, type, classroom);
    }

    public List<Device> findBySchoolAndClassroom(Long schoolId, Long classroomId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + classroomId));
        return deviceRepository.findBySchoolAndClassroom(school, classroom);
    }

    public List<Device> findByClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + classroomId));
        return deviceRepository.findByClassroom(classroom);
    }

    public void saveDevicesFromExcel(MultipartFile file, Long schoolId) throws Exception {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new IllegalArgumentException("학교를 찾을 수 없습니다."));
        List<Device> devices = new ArrayList<>();
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            boolean first = true;
            for (Row row : sheet) {
                if (first) { first = false; continue; } // 헤더 스킵
                
                // 빈 행 체크 - 첫 번째 컬럼과 타입(3번째 컬럼) 모두 비어있으면 스킵
                if (isEmptyRow(row)) continue;
                
                // UID 정보 처리 (첫 번째 컬럼)
                String uidInfo = getCellString(row.getCell(0));
                String uidCate = null;
                
                // 관리번호는 두 번째 컬럼(1)
                String manageNo = getCellString(row.getCell(1));
                // Manage 엔티티 조회/생성 (관리번호가 없으면 null)
                Manage manage = null;
                if (manageNo != null && !manageNo.trim().isEmpty()) {
                    ManageNumber mn = parseManageNo(manageNo);
                    manage = manageRepository.findByManageCateAndYearAndManageNum(mn.manageCate, mn.year, mn.manageNum)
                        .orElseGet(() -> {
                            Manage m = new Manage();
                            m.setManageCate(mn.manageCate);
                            m.setYear(mn.year);
                            m.setManageNum(mn.manageNum);
                            return manageRepository.save(m);
                        });
                }
                
                // 타입 정보 (세 번째 컬럼)
                String type = getCellString(row.getCell(2));
                
                // 유효한 타입이 없으면 스킵
                if (type == null || type.trim().isEmpty()) continue;
                
                // UID 카테고리 결정 로직
                if (uidInfo == null || uidInfo.trim().isEmpty()) {
                    // UID 정보가 비어있을 경우 자동 생성
                    if (type != null && !type.trim().isEmpty()) {
                        if ("데스크톱".equals(type)) {
                            // 데스크톱의 경우 manageCate에 따라 UID 카테고리 결정
                            if (manage != null && manage.getManageCate() != null) {
                                String manageCate = manage.getManageCate();
                                switch (manageCate) {
                                    case "업무":
                                        uidCate = "DW";
                                        break;
                                    case "교육":
                                        uidCate = "DE";
                                        break;
                                    case "기타":
                                        uidCate = "DK";
                                        break;
                                    case "컴퓨터교육":
                                        uidCate = "DC";
                                        break;
                                    case "학교구매":
                                        uidCate = "DS";
                                        break;
                                    case "기증품":
                                        uidCate = "DD";
                                        break;
                                    default:
                                        uidCate = "DW"; // 기본값
                                        break;
                                }
                            } else {
                                uidCate = "DW"; // 관리번호가 없는 경우 기본값
                            }
                        } else {
                            // 다른 장비 타입에 따라 UID 카테고리 결정
                            switch (type) {
                                case "모니터":
                                    uidCate = "MO";
                                    break;
                                case "프린터":
                                    uidCate = "PR";
                                    break;
                                case "TV":
                                    uidCate = "TV";
                                    break;
                                case "전자칠판":
                                    uidCate = "ID";
                                    break;
                                case "전자교탁":
                                    uidCate = "ED";
                                    break;
                                case "DID":
                                    uidCate = "DI";
                                    break;
                                case "태블릿":
                                    uidCate = "TB";
                                    break;
                                case "프로젝트":
                                case "프로젝터":
                                    uidCate = "PJ";
                                    break;
                                default:
                                    uidCate = "ET"; // 기타 장비
                                    break;
                            }
                        }
                    } else {
                        uidCate = "ET"; // 타입 정보가 없는 경우 기본값
                    }
                } else {
                    // UID 정보가 직접 입력된 경우 그대로 사용
                    uidCate = uidInfo;
                }
                
                // Operator(취급자/직위) 컬럼 인덱스 조정
                String operatorPosition = getCellString(row.getCell(3)); // 직위
                String operatorName = getCellString(row.getCell(4)); // 취급자
                Operator operator = null;
                if (operatorName != null && !operatorName.isBlank() && operatorPosition != null && !operatorPosition.isBlank()) {
                    operator = operatorService.findByNameAndPositionAndSchool(operatorName, operatorPosition, school)
                        .orElseGet(() -> {
                            Operator op = new Operator();
                            op.setName(operatorName);
                            op.setPosition(operatorPosition);
                            op.setSchool(school);
                            return operatorService.saveOperator(op);
                        });
                }
                // Classroom(설치장소) 컬럼 인덱스 조정
                String classroomName = getCellString(row.getCell(9));
                Classroom classroom = null;
                if (classroomName != null && !classroomName.isBlank()) {
                    classroom = classroomService.findByRoomName(classroomName.trim());
                    if (classroom == null) {
                        classroom = new Classroom();
                        classroom.setRoomName(classroomName.trim());
                        classroom.setSchool(school);
                        classroom.setXCoordinate(0);
                        classroom.setYCoordinate(0);
                        classroom.setWidth(100);
                        classroom.setHeight(100);
                        classroom = classroomService.saveClassroom(classroom);
                    }
                }
                Device device = new Device();
                device.setManage(manage);
                // Device 필드 매핑 순서 조정
                device.setType(getCellString(row.getCell(2)));
                device.setManufacturer(getCellString(row.getCell(5)));
                device.setModelName(getCellString(row.getCell(6)));
                device.setPurchaseDate(parseLocalDate(row.getCell(7)));
                device.setIpAddress(getCellString(row.getCell(8)));
                device.setClassroom(classroom);
                device.setPurpose(getCellString(row.getCell(10)));
                device.setUnused(false);
                device.setSetType(getCellString(row.getCell(11)));
                device.setNote(getCellString(row.getCell(12)));
                device.setSchool(school); // 반드시 선택한 학교로 저장
                device.setOperator(operator);
                
                // 디바이스 저장 전에 UID 설정 - 필수 데이터 확인
                if (uidCate != null && !uidCate.trim().isEmpty()) {
                    devices.add(device);
                }
            }
        }
        
        // 모든 디바이스 추출 후, UID 카테고리별로 그룹화하여 ID 번호 부여
        Map<String, List<Device>> devicesByCate = devices.stream()
                .collect(Collectors.groupingBy(device -> {
                    // Device에 설정된 UID 카테고리 얻기
                    String type = device.getType();
                    Manage manage = device.getManage();
                    
                    if ("데스크톱".equals(type)) {
                        if (manage != null && manage.getManageCate() != null) {
                            String manageCate = manage.getManageCate();
                            switch (manageCate) {
                                case "업무": return "DW";
                                case "교육": return "DE";
                                case "기타": return "DK";
                                case "컴퓨터교육": return "DC";
                                case "학교구매": return "DS";
                                case "기증품": return "DD";
                                default: return "DW";
                            }
                        } else {
                            return "DW";
                        }
                    } else {
                        switch (type) {
                            case "모니터": return "MO";
                            case "프린터": return "PR";
                            case "TV": return "TV";
                            case "전자칠판": return "ID";
                            case "전자교탁": return "ED";
                            case "DID": return "DI";
                            case "태블릿": return "TB";
                            case "프로젝트":
                            case "프로젝터": return "PJ";
                            default: return "ET";
                        }
                    }
                }));
        
        // 각 카테고리별로 ID 번호 부여하고 UID 생성
        for (Map.Entry<String, List<Device>> entry : devicesByCate.entrySet()) {
            String cate = entry.getKey();
            List<Device> deviceList = entry.getValue();
            
            // 현재 카테고리의 최대 ID 번호 조회
            Long lastNumber = school != null 
                ? uidService.getLastIdNumberBySchool(cate, school) 
                : uidService.getLastIdNumber(cate);
            
            // 각 장비에 순차적으로 ID 번호 부여
            for (int i = 0; i < deviceList.size(); i++) {
                Long idNumber = lastNumber + i + 1;
                Device device = deviceList.get(i);
                setDeviceUidWithNumber(device, cate, idNumber);
            }
        }
        
        // 최종 저장
        deviceRepository.saveAll(devices);
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private static class ManageNumber {
        String manageCate;
        Integer year;
        Long manageNum;
        ManageNumber(String manageCate, Integer year, Long manageNum) {
            this.manageCate = manageCate;
            this.year = year;
            this.manageNum = manageNum;
        }
    }

    private ManageNumber parseManageNo(String manageNo) {
        String[] parts = manageNo.split("-");
        if (parts.length == 3) {
            return new ManageNumber(parts[0], Integer.parseInt(parts[1]), Long.parseLong(parts[2]));
        } else if (parts.length == 2) {
            return new ManageNumber(parts[0], null, Long.parseLong(parts[1]));
        } else {
            throw new IllegalArgumentException("잘못된 관리번호 형식: " + manageNo);
        }
    }

    private LocalDate parseLocalDate(Cell cell) {
        String value = getCellString(cell);
        if (value == null || value.isBlank()) return null;
        value = value.replace("년", "-").replace("월", "").replace("일", "").replace(" ", "").replace("--", "-");
        try {
            if (value.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-M-d"));
            } else if (value.matches("\\d{4}-\\d{1,2}")) {
                return LocalDate.parse(value + "-01", DateTimeFormatter.ofPattern("yyyy-M-d"));
            } else if (value.matches("\\d{4}")) {
                return LocalDate.parse(value + "-01-01", DateTimeFormatter.ofPattern("yyyy-M-d"));
            }
        } catch (DateTimeParseException e) {
            // 무시하고 null 반환
        }
        return null;
    }

    /**
     * 장비에 Uid를 설정합니다.
     * @param device 장비 객체
     * @param cate Uid 카테고리
     * @return 업데이트된 장비 객체
     */
    public Device setDeviceUid(Device device, String cate) {
        System.out.println("Creating new Uid with cate: " + cate + " for device: " + device);
        
        // 장비의 학교 정보 가져오기
        School school = device.getSchool();
        Uid uid;
        
        if (school != null) {
            // 학교 정보가 있으면 학교별로 Uid 생성
            uid = uidService.createNextUidWithSchool(cate, school);
        } else {
            // 학교 정보가 없으면 기존 방식대로 Uid 생성
            uid = uidService.createNextUid(cate);
        }
        
        device.setUid(uid);
        return deviceRepository.save(device);
    }
    
    /**
     * 장비의 Uid를 특정 값으로 설정합니다.
     * @param device 장비 객체
     * @param cate Uid 카테고리
     * @param idNumber Uid 번호
     * @return 업데이트된 장비 객체
     */
    public Device setDeviceUidWithNumber(Device device, String cate, Long idNumber) {
        System.out.println("Setting Uid with cate: " + cate + ", idNumber: " + idNumber + " for device: " + device);
        
        // 장비의 학교 정보 가져오기
        School school = device.getSchool();
        Uid uid;
        
        if (school != null) {
            // 학교별로 Uid 조회 또는 생성
            uid = uidService.findBySchoolAndCateAndIdNumber(school, cate, idNumber)
                    .orElseGet(() -> uidService.createUidWithSchool(cate, idNumber, school));
        } else {
            // 학교 정보가 없으면 기존 방식대로 처리
            uid = uidService.getUidByCateAndIdNumber(cate, idNumber)
                    .orElseGet(() -> uidService.createUid(cate, idNumber));
        }
        
        device.setUid(uid);
        return deviceRepository.save(device);
    }

    // 빈 행 여부 체크
    private boolean isEmptyRow(Row row) {
        // 최소한 타입(3번째 컬럼)은 있어야 함
        Cell typeCell = row.getCell(2);
        if (typeCell == null) return true;
        
        String typeValue = getCellString(typeCell);
        if (typeValue == null || typeValue.trim().isEmpty()) return true;
        
        // 최소한 하나의 다른 컬럼에 데이터가 있어야 함
        for (int i = 0; i <= 12; i++) {
            if (i == 2) continue; // 타입 컬럼은 이미 체크함
            
            Cell cell = row.getCell(i);
            if (cell != null) {
                String value = getCellString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false; // 데이터가 있는 컬럼을 발견
                }
            }
        }
        
        return true; // 모든 컬럼이 비어있음
    }

    /**
     * 필터링된 장비 목록을 가져오는 메서드
     * @param schoolId 학교 ID (필터링할 경우)
     * @param type 장비 유형 (필터링할 경우)
     * @param classroomId 교실 ID (필터링할 경우)
     * @return 필터링된 장비 목록
     */
    public List<Device> findFiltered(Long schoolId, String type, Long classroomId) {
        if (schoolId != null && type != null && !type.isEmpty() && classroomId != null) {
            return findBySchoolAndTypeAndClassroom(schoolId, type, classroomId);
        } else if (schoolId != null && type != null && !type.isEmpty()) {
            return findBySchoolAndType(schoolId, type);
        } else if (schoolId != null && classroomId != null) {
            return findBySchoolAndClassroom(schoolId, classroomId);
        } else if (schoolId != null) {
            return findBySchool(schoolId);
        } else if (type != null && !type.isEmpty()) {
            return findByType(type);
        } else if (classroomId != null) {
            return findByClassroom(classroomId);
        } else {
            return findAll();
        }
    }
} 