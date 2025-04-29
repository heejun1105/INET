package com.inet.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.inet.entity.Device;
import com.inet.entity.School;
import com.inet.repository.DeviceRepository;
import com.inet.repository.SchoolRepository;

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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    private final SchoolRepository schoolRepository;
    private final OperatorService operatorService;
    
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

    // 페이징 + 학교 + 타입 조건 검색
    public Page<Device> getDevices(Long schoolId, String type, Pageable pageable) {
        if (schoolId != null && type != null && !type.isEmpty()) {
            School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
            return deviceRepository.findBySchoolAndType(school, type, pageable);
        } else if (schoolId != null) {
            School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
            return deviceRepository.findBySchool(school, pageable);
        } else if (type != null && !type.isEmpty()) {
            return deviceRepository.findByType(type, pageable);
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

        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "학교명", "관리번호", "유형", "제조사", "모델명", "구매일자", "IP주소", "교실", "용도", "세트분류", "운영자", "직위", "불용", "비고"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 데이터 입력
        int rowNum = 1;
        for (Device device : devices) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(device.getDeviceId());
            row.createCell(1).setCellValue(device.getSchool().getSchoolName());
            row.createCell(2).setCellValue(device.getManageNo());
            row.createCell(3).setCellValue(device.getType());
            row.createCell(4).setCellValue(device.getManufacturer());
            row.createCell(5).setCellValue(device.getModelName());
            row.createCell(6).setCellValue(device.getPurchaseDate() != null ? device.getPurchaseDate().toString() : "");
            row.createCell(7).setCellValue(device.getIpAddress());
            row.createCell(8).setCellValue(device.getClassroom() != null ? device.getClassroom().getRoomName() : "");
            row.createCell(9).setCellValue(device.getPurpose());
            row.createCell(10).setCellValue(device.getSetType());
            row.createCell(11).setCellValue(device.getOperator() != null ? device.getOperator().getName() : "");
            row.createCell(12).setCellValue(device.getOperator() != null ? device.getOperator().getPosition() : "");
            row.createCell(13).setCellValue(device.getUnused() != null && device.getUnused() ? "예" : "아니오");
            row.createCell(14).setCellValue(device.getNote());
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
} 