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
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

        // 헤더 스타일 설정
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // 이미지 기준 헤더 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = {"No", "관리번호", "종류", "직위", "취급자", "제조사", "모델명", "도입일자", "현IP주소", "설치장소", "용도,컴퓨터실,불용", "세트분류", "비고"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Device device : devices) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(""); // No 컬럼 비움
            // 관리번호
            String manageNo = "";
            Manage manage = device.getManage();
            if (manage != null) {
                String cate = manage.getManageCate() != null ? manage.getManageCate() : "";
                String year = manage.getYear() != null ? manage.getYear().toString() : "";
                String num = manage.getManageNum() != null ? manage.getManageNum().toString() : "";
                manageNo = (cate + (year.isEmpty() ? "" : ("-" + year)) + (num.isEmpty() ? "" : ("-" + num))).replaceAll("-$", "");
            }
            row.createCell(1).setCellValue(manageNo);
            // 종류
            row.createCell(2).setCellValue(device.getType());
            // 직위
            row.createCell(3).setCellValue(device.getOperator() != null ? device.getOperator().getPosition() : "");
            // 취급자
            row.createCell(4).setCellValue(device.getOperator() != null ? device.getOperator().getName() : "");
            // 제조사
            row.createCell(5).setCellValue(device.getManufacturer());
            // 모델명
            row.createCell(6).setCellValue(device.getModelName());
            // 도입일자
            row.createCell(7).setCellValue(device.getPurchaseDate() != null ? device.getPurchaseDate().toString() : "");
            // 현IP주소
            row.createCell(8).setCellValue(device.getIpAddress());
            // 설치장소
            row.createCell(9).setCellValue(device.getClassroom() != null ? device.getClassroom().getRoomName() : "");
            // 용도,컴퓨터실,불용 컬럼에는 오직 용도만 출력
            row.createCell(10).setCellValue(device.getPurpose() != null ? device.getPurpose() : "");
            // 세트분류
            row.createCell(11).setCellValue(device.getSetType());
            // 비고
            row.createCell(12).setCellValue(device.getNote());
        }

        // 컬럼 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
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
                if (row.getCell(0) == null) continue;
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
                devices.add(device);
            }
        }
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
} 