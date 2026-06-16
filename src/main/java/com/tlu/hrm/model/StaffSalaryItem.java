package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_staff_salary_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffSalaryItem extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salary_item_id", nullable = false)
    private SalaryItem salaryItem;

    @Column(name = "amount", nullable = false)
    private Double amount = 0.0;
}
