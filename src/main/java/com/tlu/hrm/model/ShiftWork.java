package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "tbl_shift_work")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShiftWork extends BaseModel {

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "work_ratio", nullable = false)
    private Double workRatio;

    @Column(name = "description")
    private String description;
}
