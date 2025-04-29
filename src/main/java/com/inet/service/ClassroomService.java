package com.inet.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.inet.entity.Classroom;
import com.inet.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassroomService {
    
    private final ClassroomRepository classroomRepository;
    
    public Classroom saveClassroom(Classroom classroom) {
        try {
            log.info("Saving classroom: {}", classroom);
            if (classroom.getRoomName() == null || classroom.getRoomName().trim().isEmpty()) {
                throw new IllegalArgumentException("교실 이름이 필요합니다.");
            }
            if (classroom.getSchool() == null) {
                throw new IllegalArgumentException("학교 정보가 필요합니다.");
            }
            return classroomRepository.save(classroom);
        } catch (Exception e) {
            log.error("교실 저장 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("교실 저장 중 오류가 발생했습니다.", e);
        }
    }
    
    public List<Classroom> getAllClassrooms() {
        log.info("Getting all classrooms");
        return classroomRepository.findAll();
    }
    
    public Optional<Classroom> getClassroomById(Long id) {
        log.info("Getting classroom by id: {}", id);
        return classroomRepository.findById(id);
    }
    
    public Classroom updateClassroom(Classroom classroom) {
        log.info("Updating classroom: {}", classroom);
        return classroomRepository.save(classroom);
    }
    
    public void deleteClassroom(Long id) {
        log.info("Deleting classroom with id: {}", id);
        classroomRepository.deleteById(id);
    }
    
    public List<Classroom> findBySchoolId(Long schoolId) {
        return classroomRepository.findBySchoolSchoolId(schoolId);
    }
    
    public Classroom findByRoomName(String roomName) {
        return classroomRepository.findByRoomName(roomName);
    }
} 