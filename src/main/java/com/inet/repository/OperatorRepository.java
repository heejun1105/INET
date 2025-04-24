package com.inet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.inet.entity.Operator;

public interface OperatorRepository extends JpaRepository<Operator, Long> {
} 