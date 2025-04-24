package com.inet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.inet.entity.TestEntity;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
} 