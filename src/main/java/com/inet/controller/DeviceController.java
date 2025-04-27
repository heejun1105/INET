package com.inet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.inet.entity.Device;
import com.inet.entity.Operator;
import com.inet.entity.School;
import com.inet.service.DeviceService;
import com.inet.service.SchoolService;
import com.inet.service.OperatorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/device")
@RequiredArgsConstructor
@Log4j2
public class DeviceController {

    private final DeviceService deviceService;
    private final SchoolService schoolService;
    private final OperatorService operatorService;

    @GetMapping("/list")
    public String list(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        model.addAttribute("schools", schoolService.getAllSchools());
        model.addAttribute("types", deviceService.getAllTypes());
        model.addAttribute("selectedSchoolId", schoolId);
        model.addAttribute("selectedType", type);

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
        return "device/register";
    }

    @PostMapping("/register")
    public String register(Device device, String operatorName, String operatorPosition) {
        log.info("Registering device: {}", device);
        
        // 학교 정보 가져오기
        School school = device.getSchool();
        
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
        return "device/modify";
    }

    @PostMapping("/modify")
    public String modify(Device device, String operatorName, String operatorPosition) {
        log.info("Modifying device: {}", device);
        
        // 학교 정보 가져오기
        School school = device.getSchool();
        
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
    public String remove(Long deviceId) {
        log.info("Removing device: {}", deviceId);
        deviceService.deleteDevice(deviceId);
        return "redirect:/device/list";
    }
} 