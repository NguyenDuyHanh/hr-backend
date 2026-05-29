package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
import java.time.LocalDate;

@Entity
@Table(name = "tbl_staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Staff extends BaseModel {

    @Column(name = "staff_code", unique = true, nullable = false)
    private String staffCode;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "gender")
    private String gender;

    @Column(name = "working_status")
    private String workingStatus;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "recruitment_date")
    private LocalDate recruitmentDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @Column(name = "current_address")
    private String currentAddress;

    @Column(name = "social_insurance_code")
    private String socialInsuranceCode;

    @Column(name = "level")
    private String level;

}
