package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.tlu.hrm.enums.EducationDegree;
import com.tlu.hrm.enums.Gender;
import com.tlu.hrm.enums.WorkingStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "working_status")
    private WorkingStatus workingStatus;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ethnic_id")
    private Ethnic ethnic;

    @Column(name = "religion")
    private String religion;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_degree")
    private EducationDegree educationDegree;

    // 2. Address
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permanent_administrative_unit_id")
    private AdministrativeUnit permanentAdministrativeUnit;

    @Column(name = "permanent_address_detail")
    private String permanentAddressDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_administrative_unit_id")
    private AdministrativeUnit currentAdministrativeUnit;

    @Column(name = "current_address_detail")
    private String currentAddressDetail;

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

    @Column(name = "annual_leave")
    private Double annualLeave = 12.0;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffCertificate> certificates = new ArrayList<>();

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffBankAccount> bankAccounts = new ArrayList<>();
}
