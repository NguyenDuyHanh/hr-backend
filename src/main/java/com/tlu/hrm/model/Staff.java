package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    // 1. Personal info
    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "birth_place")
    private String birthPlace;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "ethnics")
    private String ethnics;

    @Column(name = "religion")
    private String religion;

    @Column(name = "education_degree")
    private String educationDegree;

    // 2. Address
    @Column(name = "province")
    private String province;

    @Column(name = "commune")
    private String commune;

    @Column(name = "permanent_residence")
    private String permanentResidence;

    @Column(name = "current_residence")
    private String currentResidence;

    // 3. Legal docs
    @Column(name = "id_number_issue_date")
    private LocalDate idNumberIssueDate;

    @Column(name = "id_number_issue_by")
    private String idNumberIssueBy;

    @Column(name = "company_email")
    private String companyEmail;

    // 5. Tax & Insurance
    @Column(name = "tax_code")
    private String taxCode;

    @Column(name = "health_insurance_number")
    private String healthInsuranceNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_account_name")
    private String bankAccountName;

    @Column(name = "bank_bin")
    private String bankBin;

    @Column(name = "annual_leave")
    private Double annualLeave = 12.0;
}
