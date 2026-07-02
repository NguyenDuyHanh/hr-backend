package com.tlu.hrm.model;

import com.tlu.hrm.enums.PaidStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_payslip")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payslip extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private Payroll payroll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "total_work_days", nullable = false)
    private Double totalWorkDays = 0.0;

    @Column(name = "total_ot_hours", nullable = false)
    private Double totalOtHours = 0.0;

    @Column(name = "total_weekend_ot_hours", nullable = false)
    private Double totalWeekendOtHours = 0.0;

    @Column(name = "total_holiday_ot_hours", nullable = false)
    private Double totalHolidayOtHours = 0.0;

    @Column(name = "total_income", nullable = false)
    private Double totalIncome = 0.0;

    @Column(name = "total_deduction", nullable = false)
    private Double totalDeduction = 0.0;

    @Column(name = "net_salary", nullable = false)
    private Double netSalary = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "paid_status", nullable = false)
    private PaidStatus paidStatus = PaidStatus.UNPAID;

    @Column(name = "note")
    private String note;

    @OneToMany(mappedBy = "payslip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayslipItem> items = new ArrayList<>();
}
