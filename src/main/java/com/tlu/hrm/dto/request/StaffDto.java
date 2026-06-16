package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.Staff;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffDto {
    private UUID id;
    private String staffCode;
    private String displayName;
    private LocalDate birthDate;
    private String gender;
    private String phoneNumber;
    private String email;
    private String workingStatus;
    private String idNumber;
    private LocalDate recruitmentDate;
    private LocalDate startDate;
    private String currentAddress;
    private String socialInsuranceCode;
    private String level;
    
    // IDs for linked entities
    private UUID departmentId;
    private String departmentName;
    private UUID positionId;
    private String positionName;

    // --- Expanded general_info_fields.md fields ---
    private String imagePath;
    private String maritalStatus;
    private String birthPlace;
    private String nationalityId;
    private String ethnicsId;
    private String religionId;
    private String educationDegreeId;

    private String provinceId;
    private String administrativeunitId;
    private String permanentResidence;
    private String currentResidence;
    private String homeTown;

    private LocalDate idNumberIssueDate;
    private String idNumberIssueBy;
    private String personalIdentificationNumber;
    private LocalDate personalIdentificationIssueDate;
    private String personalIdentificationIssuePlace;
    private String passportNumber;
    private String workPermitNumber;

    private String statusId;
    private String staffWorkingFormat;
    private String introducerId;
    private String recruiterId;
    private Integer apprenticeDays;
    private String companyEmail;
    private String staffPhase;
    private String staffPositionType;
    private String healthCareRegistrationPlaceId;
    private String staffWorkShiftType;
    private String fixShiftWorkId;
    private String staffLeaveShiftType;
    private String fixLeaveWeekDay;
    private String fixLeaveWeekDay2;

    private Boolean skipTimekeeping;
    private Boolean skipLateEarlyCount;
    private Boolean skipOvertimeCount;
    private Boolean onBlacklist;
    private Boolean hasSocialIns;
    private Boolean unemploymentDeclaration;
    private Boolean allowExternalIpTimekeeping;

    private String organizationId;
    private String positionTitleId;

    private String contactPersonInfo;

    private String taxCode;
    private String socialInsuranceNumber;
    private String healthInsuranceNumber;
    private String socialInsuranceNote;

    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String bankBin;

    public StaffDto(Staff entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.staffCode = entity.getStaffCode();
            this.displayName = entity.getDisplayName();
            this.birthDate = entity.getBirthDate();
            this.gender = entity.getGender();
            this.phoneNumber = entity.getPhoneNumber();
            this.email = entity.getEmail();
            this.workingStatus = entity.getWorkingStatus();
            this.idNumber = entity.getIdNumber();
            this.recruitmentDate = entity.getRecruitmentDate();
            this.startDate = entity.getStartDate();
            this.currentAddress = entity.getCurrentAddress();
            this.socialInsuranceCode = entity.getSocialInsuranceCode();
            this.level = entity.getLevel();
            if (entity.getDepartment() != null) {
                this.departmentId = entity.getDepartment().getId();
                this.departmentName = entity.getDepartment().getName();
            }
            if (entity.getPosition() != null) {
                this.positionId = entity.getPosition().getId();
                this.positionName = entity.getPosition().getName();
            }
            
            // Map expanded fields
            this.imagePath = entity.getImagePath();
            this.maritalStatus = entity.getMaritalStatus();
            this.birthPlace = entity.getBirthPlace();
            this.nationalityId = entity.getNationalityId();
            this.ethnicsId = entity.getEthnicsId();
            this.religionId = entity.getReligionId();
            this.educationDegreeId = entity.getEducationDegreeId();
            this.provinceId = entity.getProvinceId();
            this.administrativeunitId = entity.getAdministrativeunitId();
            this.permanentResidence = entity.getPermanentResidence();
            this.currentResidence = entity.getCurrentResidence();
            this.homeTown = entity.getHomeTown();
            this.idNumberIssueDate = entity.getIdNumberIssueDate();
            this.idNumberIssueBy = entity.getIdNumberIssueBy();
            this.personalIdentificationNumber = entity.getPersonalIdentificationNumber();
            this.personalIdentificationIssueDate = entity.getPersonalIdentificationIssueDate();
            this.personalIdentificationIssuePlace = entity.getPersonalIdentificationIssuePlace();
            this.passportNumber = entity.getPassportNumber();
            this.workPermitNumber = entity.getWorkPermitNumber();
            this.statusId = entity.getStatusId();
            this.staffWorkingFormat = entity.getStaffWorkingFormat();
            this.introducerId = entity.getIntroducerId();
            this.recruiterId = entity.getRecruiterId();
            this.apprenticeDays = entity.getApprenticeDays();
            this.companyEmail = entity.getCompanyEmail();
            this.staffPhase = entity.getStaffPhase();
            this.staffPositionType = entity.getStaffPositionType();
            this.healthCareRegistrationPlaceId = entity.getHealthCareRegistrationPlaceId();
            this.staffWorkShiftType = entity.getStaffWorkShiftType();
            this.fixShiftWorkId = entity.getFixShiftWorkId();
            this.staffLeaveShiftType = entity.getStaffLeaveShiftType();
            this.fixLeaveWeekDay = entity.getFixLeaveWeekDay();
            this.fixLeaveWeekDay2 = entity.getFixLeaveWeekDay2();
            this.skipTimekeeping = entity.getSkipTimekeeping();
            this.skipLateEarlyCount = entity.getSkipLateEarlyCount();
            this.skipOvertimeCount = entity.getSkipOvertimeCount();
            this.onBlacklist = entity.getOnBlacklist();
            this.hasSocialIns = entity.getHasSocialIns();
            this.unemploymentDeclaration = entity.getUnemploymentDeclaration();
            this.allowExternalIpTimekeeping = entity.getAllowExternalIpTimekeeping();
            this.organizationId = entity.getOrganizationId();
            this.positionTitleId = entity.getPositionTitleId();
            this.contactPersonInfo = entity.getContactPersonInfo();
            this.taxCode = entity.getTaxCode();
            this.socialInsuranceNumber = entity.getSocialInsuranceNumber();
            this.healthInsuranceNumber = entity.getHealthInsuranceNumber();
            this.socialInsuranceNote = entity.getSocialInsuranceNote();
            this.bankName = entity.getBankName();
            this.bankAccountNumber = entity.getBankAccountNumber();
            this.bankAccountName = entity.getBankAccountName();
            this.bankBin = entity.getBankBin();
        }
    }
}
