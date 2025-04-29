package com.inet.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.inet.config.Views;

@Entity
@Table(name = "classroom")
@Data
public class Classroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "classroom_id")
    @JsonView(Views.Summary.class)
    private Long classroomId;

    @Column(name = "room_name")
    @JsonView(Views.Summary.class)
    private String roomName;

    @Column(name = "x_coordinate")
    @JsonProperty("xCoordinate")
    @JsonView(Views.Summary.class)
    private Integer xCoordinate;

    @Column(name = "y_coordinate")
    @JsonProperty("yCoordinate")
    @JsonView(Views.Summary.class)
    private Integer yCoordinate;

    @Column(name = "width", columnDefinition = "INT DEFAULT 100")
    @JsonView(Views.Summary.class)
    private Integer width = 100;

    @Column(name = "height", columnDefinition = "INT DEFAULT 100")
    @JsonView(Views.Summary.class)
    private Integer height = 100;

    @ManyToOne
    @JoinColumn(name = "school_id")
    @JsonView(Views.Detail.class)
    private School school;

    @OneToMany(mappedBy = "classroom")
    @JsonManagedReference
    @JsonView(Views.Detail.class)
    private List<Device> devices;
} 