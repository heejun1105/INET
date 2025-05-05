package com.inet.controller;

import com.inet.repository.ManageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/manage")
@RequiredArgsConstructor
public class ManageApiController {
    private final ManageRepository manageRepository;

    @GetMapping("/cates")
    public List<String> getAllCates() {
        return manageRepository.findDistinctManageCate();
    }

    @GetMapping("/years")
    public List<Integer> getAllYears() {
        return manageRepository.findDistinctYear();
    }

    @GetMapping("/next-num")
    public Long getNextNum(@RequestParam String cate, @RequestParam Integer year) {
        Long maxNum = manageRepository.findMaxManageNumByCateAndYear(cate, year);
        return (maxNum == null) ? 1L : maxNum + 1;
    }
} 