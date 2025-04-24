package com.inet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.inet.entity.School;

public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findBySchoolId(Long schoolId);
} 