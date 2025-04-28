package com.inet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;

@Entity
@Table(name = "school")
@Data
public class School {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "school_id")
    private Long schoolId;
    
    @Column(name = "school_name")
    private String schoolName;
    
    private Integer ip;

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public Integer getIp() {
        return ip;
    }

    public void setIp(Integer ip) {
        this.ip = ip;
    }

    public void setName(String name) {
        this.schoolName = name;
    }

    public void setAddress(String address) {
        // This field is not present in the entity
    }

    public void setPhone(String phone) {
        // This field is not present in the entity
    }
} 