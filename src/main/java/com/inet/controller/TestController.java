package com.inet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.inet.entity.TestEntity;
import com.inet.repository.TestRepository;

@RestController
public class TestController {
    
	/*
	 * @Autowired private TestRepository testRepository;
	 * 
	 * @GetMapping("/test") public String test() { TestEntity entity = new
	 * TestEntity(); entity.setName("test"); testRepository.save(entity); return
	 * "Connection test successful!"; }
	 */
} 