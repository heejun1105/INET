package com.inet.service;

import com.inet.entity.Manage;
import com.inet.entity.School;
import com.inet.repository.ManageRepository;
import com.inet.repository.SchoolRepository;
import com.inet.repository.DeviceRepository;
import com.inet.entity.Device;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManageService {
    private final ManageRepository manageRepository;
    private final SchoolRepository schoolRepository;
    private final DeviceRepository deviceRepository;

    public List<String> getManageCatesBySchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new IllegalArgumentException("School not found"));
        return manageRepository.findDistinctManageCateBySchool(school);
    }

    public List<Integer> getYearsBySchoolAndManageCate(Long schoolId, String manageCate) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new IllegalArgumentException("School not found"));
        return manageRepository.findDistinctYearBySchoolAndManageCate(school, manageCate);
    }

    public Long getNextManageNum(Long schoolId, String manageCate, Integer year) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new IllegalArgumentException("School not found"));
        
        List<Manage> manages;
        if (year != null) {
            manages = manageRepository.findBySchoolAndManageCateAndYearOrderByManageNumDesc(school, manageCate, year);
        } else {
            manages = manageRepository.findBySchoolAndManageCateAndYearIsNullOrderByManageNumDesc(school, manageCate);
        }
        
        return manages.isEmpty() ? 1L : manages.get(0).getManageNum() + 1;
    }

    @Transactional
    public Manage findOrCreate(School school, String cate, Integer year, Long num) {
        return manageRepository.findAll().stream()
            .filter(m -> m.getSchool().equals(school) && 
                        m.getManageCate().equals(cate) && 
                        (year == null ? m.getYear() == null : m.getYear().equals(year)) && 
                        m.getManageNum().equals(num))
            .findFirst()
            .orElseGet(() -> {
                Manage m = new Manage();
                m.setSchool(school);
                m.setManageCate(cate);
                m.setYear(year);
                m.setManageNum(num);
                return manageRepository.save(m);
            });
    }

    // 학교별 Manage 목록 조회
    public List<Manage> findBySchoolId(Long schoolId) {
        // Device에서 schoolId로 Manage 추출 (중복 제거)
        return deviceRepository.findBySchoolSchoolId(schoolId).stream()
            .map(Device::getManage)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .toList();
    }
} 