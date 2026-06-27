package com.tlu.hrm.model;

import com.tlu.hrm.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_payroll")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payroll extends BaseModel {

    @Column(name = "code")
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private Period period;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PayrollStatus status = PayrollStatus.DRAFT;

    @Column(name = "description")
    private String description;
}
