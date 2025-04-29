package com.inet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.inet.entity.Device;
import com.inet.entity.Operator;
import com.inet.entity.School;
import com.inet.entity.Classroom;
import com.inet.service.DeviceService;
import com.inet.service.SchoolService;
import com.inet.service.OperatorService;
import com.inet.service.ClassroomService;
import com.inet.config.Views;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final SchoolService schoolService;
    private final OperatorService operatorService;
    private final ClassroomService classroomService;

    @GetMapping("/list")
    public String list(@RequestParam(required = false) Long schoolId,
                      @RequestParam(required = false) String type,
                      @RequestParam(required = false) String roomName,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "10") int size,
                      Model model) {
        List<Device> devices;
        if (schoolId != null) {
            devices = deviceService.findBySchoolId(schoolId);
        } else if (type != null && !type.isEmpty()) {
            devices = deviceService.findByType(type);
        } else if (roomName != null && !roomName.isEmpty()) {
            devices = deviceService.findByClassroomRoomName(roomName);
        } else {
            devices = deviceService.findAll();
        }
        
        model.addAttribute("schools", schoolService.getAllSchools());
        model.addAttribute("types", deviceService.getAllTypes());
        model.addAttribute("selectedSchoolId", schoolId);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedRoomName", roomName);

        Pageable pageable = PageRequest.of(page, size);
        Page<Device> devicePage = deviceService.getDevices(schoolId, type, pageable);
        model.addAttribute("devicePage", devicePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "device/list";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("device", new Device());
        model.addAttribute("schools", schoolService.getAllSchools());
        model.addAttribute("classrooms", classroomService.getAllClassrooms());
        return "device/register";
    }

    @PostMapping("/register")
    public String register(Device device, String operatorName, String operatorPosition, String location) {
        log.info("Registering device: {}", device);
        
        // 학교 정보 가져오기
        School school = device.getSchool();
        if (school == null) {
            throw new IllegalArgumentException("학교 정보가 필요합니다.");
        }
        
        // 위치 정보로 교실 생성 또는 찾기
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("위치 정보가 필요합니다.");
        }
        
        Classroom classroom = classroomService.findByRoomName(location);
        if (classroom == null) {
            classroom = new Classroom();
            classroom.setRoomName(location);
            classroom.setSchool(school);
            classroom.setXCoordinate(0);
            classroom.setYCoordinate(0);
            classroom.setWidth(100);
            classroom.setHeight(100);
            classroom = classroomService.saveClassroom(classroom);
        }
        device.setClassroom(classroom);
        
        // 담당자 정보 저장
        Operator operator = new Operator();
        operator.setName(operatorName);
        operator.setPosition(operatorPosition);
        operator.setSchool(school); // 학교 정보 설정
        operatorService.saveOperator(operator);
        
        // 장비에 담당자 정보 연결
        device.setOperator(operator);
        
        deviceService.saveDevice(device);
        return "redirect:/device/list";
    }

    @GetMapping("/modify/{id}")
    public String modifyForm(@PathVariable Long id, Model model) {
        model.addAttribute("device", deviceService.getDeviceById(id).orElseThrow());
        model.addAttribute("schools", schoolService.getAllSchools());
        model.addAttribute("classrooms", classroomService.getAllClassrooms());
        return "device/modify";
    }

    @PostMapping("/modify")
    public String modify(Device device, String operatorName, String operatorPosition, String location) {
        log.info("Modifying device: {}", device);
        
        // 학교 정보 가져오기
        School school = device.getSchool();
        
        // 위치 정보로 교실 생성 또는 찾기
        Classroom classroom = classroomService.findByRoomName(location);
        if (classroom == null) {
            classroom = new Classroom();
            classroom.setRoomName(location);
            classroom.setSchool(school);
            classroom.setXCoordinate(0);
            classroom.setYCoordinate(0);
            classroom.setWidth(100);
            classroom.setHeight(100);
            classroom = classroomService.saveClassroom(classroom);
        }
        device.setClassroom(classroom);
        
        // 담당자 정보 업데이트
        Operator operator = device.getOperator();
        if (operator != null) {
            operator.setName(operatorName);
            operator.setPosition(operatorPosition);
            operator.setSchool(school); // 학교 정보 업데이트
            operatorService.updateOperator(operator);
        } else {
            operator = new Operator();
            operator.setName(operatorName);
            operator.setPosition(operatorPosition);
            operator.setSchool(school); // 학교 정보 설정
            operatorService.saveOperator(operator);
            device.setOperator(operator);
        }
        
        deviceService.updateDevice(device);
        return "redirect:/device/list";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam("device_id") Long deviceId) {
        log.info("Removing device: {}", deviceId);
        deviceService.deleteDevice(deviceId);
        return "redirect:/device/list";
    }

    @GetMapping("/excel")
    public void downloadExcel(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String type,
            HttpServletResponse response) throws IOException {
        List<Device> devices;
        
        if (schoolId != null && type != null && !type.isEmpty()) {
            devices = deviceService.findBySchoolAndType(schoolId, type);
        } else if (schoolId != null) {
            devices = deviceService.findBySchool(schoolId);
        } else if (type != null && !type.isEmpty()) {
            devices = deviceService.findByType(type);
        } else {
            devices = deviceService.findAll();
        }
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=devices.xlsx");
        
        deviceService.exportToExcel(devices, response.getOutputStream());
    }

    @GetMapping("/map")
    public String showMap(Model model) {
        model.addAttribute("schools", schoolService.getAllSchools());
        return "device/map";
    }

    @GetMapping("/api/classrooms/{schoolId}")
    @ResponseBody
    @JsonView(Views.Summary.class)
    public List<Classroom> getClassrooms(@PathVariable Long schoolId) {
        return classroomService.findBySchoolId(schoolId);
    }

    @GetMapping("/api/devices/{schoolId}")
    @ResponseBody
    public List<Device> getDevicesBySchool(@PathVariable Long schoolId) {
        return deviceService.findBySchool(schoolId);
    }

    @PostMapping("/api/save-layout")
    @ResponseBody
    public ResponseEntity<?> saveLayout(@RequestBody Map<String, Object> request) {
        try {
            Long schoolId = Long.parseLong(request.get("schoolId").toString());
            List<Map<String, Object>> rooms = (List<Map<String, Object>>) request.get("rooms");
            
            // 각 교실의 위치 정보를 저장
            for (Map<String, Object> room : rooms) {
                String roomName = (String) room.get("name");
                Map<String, Object> position = (Map<String, Object>) room.get("position");
                
                // 교실 정보 업데이트
                Classroom classroom = classroomService.findByRoomName(roomName);
                if (classroom != null) {
                    classroom.setXCoordinate((Integer) position.get("x"));
                    classroom.setYCoordinate((Integer) position.get("y"));
                    classroom.setWidth((Integer) position.get("width"));
                    classroom.setHeight((Integer) position.get("height"));
                    classroomService.updateClassroom(classroom);
                }
            }
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("레이아웃 저장 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 