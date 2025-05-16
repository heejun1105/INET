package com.inet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "uid")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Uid {
    
    @Id //고유번호 엔티티
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uid_id")
    private Long uidId;
    
    @Column(name = "cate", nullable = false)
    private String cate;
    
    @Column(name = "id_number")
    private Long idNumber;

    @Column(name = "mfg_year")
    private String mfgYear;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;
    
} 