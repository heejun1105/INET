package com.inet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "device")
@Getter
@Setter
@ToString(exclude = {"classroom"})
@EqualsAndHashCode(exclude = {"classroom"})
public class Device {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long deviceId;
    
    private String type;
    
    private String manufacturer;
    
    @Column(name = "model_name")
    private String modelName;
    
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @ManyToOne
    @JoinColumn(name = "classroom_id")
    @JsonBackReference
    private Classroom classroom;
    
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
    @JoinColumn(name = "operator_id")
    private Operator operator;
    
    @ManyToOne
    @JoinColumn(name = "manage_id")
    private Manage manage;
    
    @ManyToOne
    @JoinColumn(name = "uid_id")
    private Uid uid;
} 