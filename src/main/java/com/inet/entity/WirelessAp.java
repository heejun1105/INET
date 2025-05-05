package com.inet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;

@Entity
@Table(name = "wireless_ap")
@Data
public class WirelessAp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long APId;

    @Column(name = "location")
    private String location;

    @Column(name = "new_label_number")
    private String newLabelNumber;

    @Column(name = "device_number")
    private String deviceNumber;

    @Column(name = "year")
    private Integer year;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "model")
    private String model;

    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "prev_location")
    private String prevLocation;

    @Column(name = "prev_label_number")
    private String prevLabelNumber;

    @Column(name = "note")
    private String note;

    @Column(name = "category")
    private String category;
} 