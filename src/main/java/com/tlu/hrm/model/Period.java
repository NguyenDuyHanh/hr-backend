package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "tbl_period")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Period extends BaseModel {

    @Column(name = "code")
    private String code;

    @Column(name = "name", nullable = false)
    private String name;


    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "standard_work_days")
    private Double standardWorkDays;

    @Column(name = "description")
    private String description;
}
