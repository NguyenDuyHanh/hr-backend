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

    // --- Expanded general_info_fields.md fields ---

    // 1. Personal info
    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "marital_status")
    private String maritalStatus;

    @Column(name = "birth_place")
    private String birthPlace;

    @Column(name = "nationality_id")
    private String nationalityId;

    @Column(name = "ethnics_id")
    private String ethnicsId;

    @Column(name = "religion_id")
    private String religionId;

    @Column(name = "education_degree_id")
    private String educationDegreeId;

    // 2. Address
    @Column(name = "province_id")
    private String provinceId;

    @Column(name = "administrativeunit_id")
    private String administrativeunitId;

    @Column(name = "permanent_residence")
    private String permanentResidence;

    @Column(name = "current_residence")
    private String currentResidence;

    @Column(name = "home_town")
    private String homeTown;

    // 3. Legal docs
    @Column(name = "id_number_issue_date")
    private LocalDate idNumberIssueDate;

    @Column(name = "id_number_issue_by")
    private String idNumberIssueBy;

    @Column(name = "personal_identification_number")
    private String personalIdentificationNumber;

    @Column(name = "personal_identification_issue_date")
    private LocalDate personalIdentificationIssueDate;

    @Column(name = "personal_identification_issue_place")
    private String personalIdentificationIssuePlace;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "work_permit_number")
    private String workPermitNumber;

    // 4. HR Profile
    @Column(name = "status_id")
    private String statusId;

    @Column(name = "staff_working_format")
    private String staffWorkingFormat;

    @Column(name = "introducer_id")
    private String introducerId;

    @Column(name = "recruiter_id")
    private String recruiterId;

    @Column(name = "apprentice_days")
    private Integer apprenticeDays;

    @Column(name = "company_email")
    private String companyEmail;

    @Column(name = "staff_phase")
    private String staffPhase;

    @Column(name = "staff_position_type")
    private String staffPositionType;

    @Column(name = "health_care_registration_place_id")
    private String healthCareRegistrationPlaceId;

    @Column(name = "staff_work_shift_type")
    private String staffWorkShiftType;

    @Column(name = "fix_shift_work_id")
    private String fixShiftWorkId;

    @Column(name = "staff_leave_shift_type")
    private String staffLeaveShiftType;

    @Column(name = "fix_leave_week_day")
    private String fixLeaveWeekDay;

    @Column(name = "fix_leave_week_day2")
    private String fixLeaveWeekDay2;

    @Column(name = "skip_timekeeping")
    private Boolean skipTimekeeping;

    @Column(name = "skip_late_early_count")
    private Boolean skipLateEarlyCount;

    @Column(name = "skip_overtime_count")
    private Boolean skipOvertimeCount;

    @Column(name = "on_blacklist")
    private Boolean onBlacklist;

    @Column(name = "has_social_ins")
    private Boolean hasSocialIns;

    @Column(name = "unemployment_declaration")
    private Boolean unemploymentDeclaration;

    @Column(name = "allow_external_ip_timekeeping")
    private Boolean allowExternalIpTimekeeping;

    // 5. Organization
    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "position_title_id")
    private String positionTitleId;

    // 6. Contact
    @Column(name = "contact_person_info")
    private String contactPersonInfo;

    // 7. Tax & Insurance
    @Column(name = "tax_code")
    private String taxCode;

    @Column(name = "social_insurance_number")
    private String socialInsuranceNumber;

    @Column(name = "health_insurance_number")
    private String healthInsuranceNumber;

    @Column(name = "social_insurance_note")
    private String socialInsuranceNote;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_account_name")
    private String bankAccountName;

    @Column(name = "bank_bin")
    private String bankBin;

    @Column(name = "annual_leave_limit")
    private Double annualLeaveLimit = 12.0;
}
