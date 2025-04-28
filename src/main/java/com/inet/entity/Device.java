package com.inet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "device")
@Data
public class Device {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long deviceId;
    
    @Column(name = "manage_no")
    private String manageNo;
    
    private String type;
    
    private String manufacturer;
    
    @Column(name = "model_name")
    private String modelName;
    
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    private String location;
    
    private String purpose;
    
    @Column(name = "set_type")
    private String setType;
    
    private Boolean unused = false;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;
    
    @ManyToOne
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;
    
    @ManyToOne
    @JoinColumn(name = "operator_id")
    private Operator operator;

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getManageNo() {
        return manageNo;
    }

    public void setManageNo(String manageNo) {
        this.manageNo = manageNo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getSetType() {
        return setType;
    }

    public void setSetType(String setType) {
        this.setType = setType;
    }

    public Boolean getUnused() {
        return unused;
    }

    public void setUnused(Boolean unused) {
        this.unused = unused;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }
} 