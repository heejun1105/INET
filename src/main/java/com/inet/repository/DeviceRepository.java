package com.inet.repository;

import com.inet.entity.Device;
import com.inet.entity.School;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findBySchool(School school);

    // 페이징 + 학교 + 타입 조건 검색
    Page<Device> findBySchoolAndType(School school, String type, Pageable pageable);
    Page<Device> findBySchool(School school, Pageable pageable);
    Page<Device> findByType(String type, Pageable pageable);
    Page<Device> findAll(Pageable pageable);

    // type 목록 조회
    @Query("SELECT DISTINCT d.type FROM Device d")
    List<String> findDistinctTypes();

    List<Device> findBySchoolSchoolId(Long schoolId);
    List<Device> findByType(String type);
    List<Device> findBySchoolSchoolIdAndType(Long schoolId, String type);

    List<Device> findByClassroomRoomName(String roomName);
} 