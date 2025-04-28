package com.inet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "classroom")
public class Classroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long classroomId;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    private String name;
    private int x;  // x 좌표
    private int y;  // y 좌표
    private int width;  // 너비
    private int height;  // 높이
} 