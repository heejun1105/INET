package com.inet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.inet.entity.Operator;
import java.util.Optional;
import com.inet.entity.School;

public interface OperatorRepository extends JpaRepository<Operator, Long> {
    Optional<Operator> findByNameAndPositionAndSchool(String name, String position, School school);
} 