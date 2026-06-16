package com.tlu.hrm.model;

import com.tlu.hrm.enums.SalaryItemType;
import com.tlu.hrm.enums.SalaryCalculationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_salary_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalaryItem extends BaseModel {

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false)
    private SalaryCalculationType calculationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private SalaryItemType type;

    @Column(name = "description")
    private String description;
}
