package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_payslip_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayslipItem extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payslip_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Payslip payslip;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salary_item_id", nullable = false)
    private SalaryItem salaryItem;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "calculated_value", nullable = false)
    private Double calculatedValue = 0.0;

    @Column(name = "amount")
    private Double amount = 0.0;
}
