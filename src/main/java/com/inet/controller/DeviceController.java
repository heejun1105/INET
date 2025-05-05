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
import com.inet.service.ManageService;
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
import com.inet.entity.Manage;

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
    private final ManageService manageService;

    @GetMapping("/list")
    public String list(@RequestParam(required = false) Long schoolId,
                      @RequestParam(required = false) String type,
                      @RequestParam(required = false) Long classroomId,
                      @RequestParam(defaultValue = "1") int page,
                      @RequestParam(defaultValue = "10") int size,
                      Model model) {
        
        model.addAttribute("schools", schoolService.getAllSchools());
        model.addAttribute("types", deviceService.getAllTypes());
        model.addAttribute("selectedSchoolId", schoolId);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedClassroomId", classroomId);

        // 선택된 학교의 교실 목록 조회
        if (schoolId != null) {
            model.addAttribute("classrooms", classroomService.findBySchoolId(schoolId));
        } else {
            model.addAttribute("classrooms", classroomService.getAllClassrooms());
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Device> devicePage = deviceService.getDevices(schoolId, type, classroomId, pageable);
        int totalPages = devicePage.getTotalPages();
        int currentPage = page;
        int startPage = ((currentPage - 1) / 10) * 10 + 1;
        int endPage = Math.min(startPage + 9, totalPages);

        model.addAttribute("devicePage", devicePage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", size);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);
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
    public String register(Device device, String operatorName, String operatorPosition, String location,
                          String manageCate, String manageCateCustom, String manageYear, String manageYearCustom, String manageNum, String manageNumCustom) {
        log.info("Registering device: {}", device);
        // 학교 정보 가져오기
        School school = device.getSchool();
        if (school == null) {
            throw new IllegalArgumentException("학교 정보가 필요합니다.");
        }
        // Operator 처리
        Operator operator = null;
        if (operatorName != null && operatorPosition != null) {
            operator = operatorService.findByNameAndPositionAndSchool(operatorName, operatorPosition, school)
                .orElseGet(() -> {
                    Operator op = new Operator();
                    op.setName(operatorName);
                    op.setPosition(operatorPosition);
                    op.setSchool(school);
                    return operatorService.saveOperator(op);
                });
        }
        device.setOperator(operator);
        // Classroom 처리
        Classroom classroom = null;
        if (location != null && !location.trim().isEmpty()) {
            classroom = classroomService.findByRoomName(location);
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
        } else {
            throw new IllegalArgumentException("위치 정보가 필요합니다.");
        }
        device.setClassroom(classroom);
        // 관리번호(Manage) 처리
        String cate = ("custom".equals(manageCate)) ? manageCateCustom : manageCate;
        Integer year = ("custom".equals(manageYear)) ? Integer.valueOf(manageYearCustom) : Integer.valueOf(manageYear);
        Long num = ("custom".equals(manageNum)) ? Long.valueOf(manageNumCustom) : Long.valueOf(manageNum);
        Manage manage = manageService.findOrCreate(device.getSchool(), cate, year, num);
        device.setManage(manage);
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
    public String modify(Device device, String operatorName, String operatorPosition, String location,
                        String manageCate, String manageCateCustom, String manageYear, String manageYearCustom, String manageNum, String manageNumCustom) {
        log.info("Modifying device: {}", device);
        School school = device.getSchool();
        // Operator 처리
        Operator operator = device.getOperator();
        if (operatorName != null && operatorPosition != null) {
            if (operator != null) {
                operator.setName(operatorName);
                operator.setPosition(operatorPosition);
                operator.setSchool(school);
                operatorService.updateOperator(operator);
            } else {
                operator = operatorService.findByNameAndPositionAndSchool(operatorName, operatorPosition, school)
                    .orElseGet(() -> {
                        Operator op = new Operator();
                        op.setName(operatorName);
                        op.setPosition(operatorPosition);
                        op.setSchool(school);
                        return operatorService.saveOperator(op);
                    });
                device.setOperator(operator);
            }
        }
        // Classroom 처리
        Classroom classroom = null;
        if (location != null && !location.trim().isEmpty()) {
            classroom = classroomService.findByRoomName(location);
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
        }
        device.setClassroom(classroom);
        // 관리번호(Manage) 처리
        String cate = ("custom".equals(manageCate)) ? manageCateCustom : manageCate;
        Integer year = ("custom".equals(manageYear)) ? Integer.valueOf(manageYearCustom) : Integer.valueOf(manageYear);
        Long num = ("custom".equals(manageNum)) ? Long.valueOf(manageNumCustom) : Long.valueOf(manageNum);
        Manage manage = manageService.findOrCreate(device.getSchool(), cate, year, num);
        device.setManage(manage);
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
            @RequestParam(required = false) Long classroomId,
            HttpServletResponse response) throws IOException {
        List<Device> devices;
        
        if (schoolId != null && type != null && !type.isEmpty() && classroomId != null) {
            devices = deviceService.findBySchoolAndTypeAndClassroom(schoolId, type, classroomId);
        } else if (schoolId != null && type != null && !type.isEmpty()) {
            devices = deviceService.findBySchoolAndType(schoolId, type);
        } else if (schoolId != null && classroomId != null) {
            devices = deviceService.findBySchoolAndClassroom(schoolId, classroomId);
        } else if (schoolId != null) {
            devices = deviceService.findBySchool(schoolId);
        } else if (type != null && !type.isEmpty()) {
            devices = deviceService.findByType(type);
        } else if (classroomId != null) {
            devices = deviceService.findByClassroom(classroomId);
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

    @GetMapping("/api/manages/{schoolId}")
    @ResponseBody
    public List<Manage> getManagesBySchool(@PathVariable Long schoolId) {
        return manageService.findBySchoolId(schoolId);
    }
} 