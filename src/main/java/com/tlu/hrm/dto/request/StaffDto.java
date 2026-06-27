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
    private LocalDate startDate;
    private String currentAddress;
    private String socialInsuranceCode;
    
    // IDs for linked entities
    private UUID departmentId;
    private String departmentName;
    private UUID positionId;
    private String positionName;

    // --- Expanded general_info_fields.md fields ---
    private String avatarUrl;
    private String birthPlace;
    private String nationality;
    private String ethnics;
    private String religion;
    private String educationDegree;

    private String province;
    private String commune;
    private String permanentResidence;
    private String currentResidence;

    private LocalDate idNumberIssueDate;
    private String idNumberIssueBy;
    private String companyEmail;

    private String taxCode;
    private String healthInsuranceNumber;

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
            this.startDate = entity.getStartDate();
            this.currentAddress = entity.getCurrentAddress();
            this.socialInsuranceCode = entity.getSocialInsuranceCode();
            if (entity.getDepartment() != null) {
                this.departmentId = entity.getDepartment().getId();
                this.departmentName = entity.getDepartment().getName();
            }
            if (entity.getPosition() != null) {
                this.positionId = entity.getPosition().getId();
                this.positionName = entity.getPosition().getName();
            }
            
            // Map expanded fields
            this.avatarUrl = entity.getAvatarUrl();
            this.birthPlace = entity.getBirthPlace();
            this.nationality = entity.getNationality();
            this.ethnics = entity.getEthnics();
            this.religion = entity.getReligion();
            this.educationDegree = entity.getEducationDegree();
            this.province = entity.getProvince();
            this.commune = entity.getCommune();
            this.permanentResidence = entity.getPermanentResidence();
            this.currentResidence = entity.getCurrentResidence();
            this.idNumberIssueDate = entity.getIdNumberIssueDate();
            this.idNumberIssueBy = entity.getIdNumberIssueBy();
            this.companyEmail = entity.getCompanyEmail();
            this.taxCode = entity.getTaxCode();
            this.healthInsuranceNumber = entity.getHealthInsuranceNumber();
            this.bankName = entity.getBankName();
            this.bankAccountNumber = entity.getBankAccountNumber();
            this.bankAccountName = entity.getBankAccountName();
            this.bankBin = entity.getBankBin();
        }
    }
}
