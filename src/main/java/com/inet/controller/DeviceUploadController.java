package com.inet.controller;

import com.inet.service.DeviceService;
import com.inet.service.SchoolService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/device/upload")
public class DeviceUploadController {
    private final SchoolService schoolService;
    private final DeviceService deviceService;

    public DeviceUploadController(SchoolService schoolService, DeviceService deviceService) {
        this.schoolService = schoolService;
        this.deviceService = deviceService;
    }

    @GetMapping
    public String showUploadForm(Model model) {
        model.addAttribute("schools", schoolService.getAllSchools());
        return "device/device_upload";
    }

    @PostMapping
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("schoolId") Long schoolId,
                                   RedirectAttributes redirectAttributes) {
        try {
            deviceService.saveDevicesFromExcel(file, schoolId);
            redirectAttributes.addFlashAttribute("message", "업로드 성공!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/device/upload";
    }
} 