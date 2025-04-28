package com.inet.service;

import com.inet.entity.Classroom;
import com.inet.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomService {
    private final ClassroomRepository classroomRepository;

    public List<Classroom> findBySchoolId(Long schoolId) {
        return classroomRepository.findBySchoolSchoolId(schoolId);
    }
} 