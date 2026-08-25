package com.tlu.hrm.dto.request;

import com.tlu.hrm.enums.EducationDegree;
import com.tlu.hrm.enums.Gender;
import com.tlu.hrm.enums.WorkingStatus;
import com.tlu.hrm.model.AdministrativeUnit;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.StaffBankAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffDto {
    private UUID id;
    private String staffCode;
    private String displayName;
    private LocalDate birthDate;
    private Gender gender;
    private String phoneNumber;
    private String email;
    private WorkingStatus workingStatus;
    private String idNumber;
    private LocalDate startDate;
    private String currentAddress;
    private String socialInsuranceCode;
    private Double annualLeave;
    
    // IDs for linked entities
    private UUID departmentId;
    private String departmentName;
    private UUID positionId;
    private String positionName;

    // Personal info
    private String avatarUrl;
    private String birthPlace;
    private String nationality;
    
    private UUID ethnicId;
    private String ethnicName;
    
    private String religion;
    private EducationDegree educationDegree;
    private String educationDegreeName;

    // Address - Permanent
    private UUID permanentAdministrativeUnitId;
    private String permanentAdministrativeUnitName;
    private String permanentDistrictName;
    private String permanentProvinceName;
    private String permanentAddressDetail;

    // Address - Current
    private UUID currentAdministrativeUnitId;
    private String currentAdministrativeUnitName;
    private String currentDistrictName;
    private String currentProvinceName;
    private String currentAddressDetail;

    // Aliases for Ward fields (backward compatibility)
    public UUID getPermanentWardId() {
        return permanentAdministrativeUnitId;
    }
    public void setPermanentWardId(UUID id) {
        this.permanentAdministrativeUnitId = id;
    }
    public String getPermanentWardName() {
        return permanentAdministrativeUnitName;
    }
    public void setPermanentWardName(String name) {
        this.permanentAdministrativeUnitName = name;
    }
    public UUID getCurrentWardId() {
        return currentAdministrativeUnitId;
    }
    public void setCurrentWardId(UUID id) {
        this.currentAdministrativeUnitId = id;
    }
    public String getCurrentWardName() {
        return currentAdministrativeUnitName;
    }
    public void setCurrentWardName(String name) {
        this.currentAdministrativeUnitName = name;
    }

    // Legal docs
    private LocalDate idNumberIssueDate;
    private String idNumberIssueBy;
    private String companyEmail;

    // Tax & Insurance
    private String taxCode;
    private String healthInsuranceNumber;

    // Default Bank Info (Convenience Fields)
    private UUID bankId;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String bankBin;

    // Certificates list
    private List<StaffCertificateDto> certificates = new ArrayList<>();

    // Bank accounts list
    private List<StaffBankAccountDto> bankAccounts = new ArrayList<>();

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
            this.annualLeave = entity.getAnnualLeave();
            
            if (entity.getDepartment() != null) {
                this.departmentId = entity.getDepartment().getId();
                this.departmentName = entity.getDepartment().getName();
            }
            if (entity.getPosition() != null) {
                this.positionId = entity.getPosition().getId();
                this.positionName = entity.getPosition().getName();
            }
            
            this.avatarUrl = entity.getAvatarUrl();
            this.birthPlace = entity.getBirthPlace();
            this.nationality = entity.getNationality();
            
            if (entity.getEthnic() != null) {
                this.ethnicId = entity.getEthnic().getId();
                this.ethnicName = entity.getEthnic().getName();
            }
            
            this.religion = entity.getReligion();
            this.educationDegree = entity.getEducationDegree();
            if (entity.getEducationDegree() != null) {
                this.educationDegreeName = entity.getEducationDegree().getDescription();
            }

            // Permanent Administrative Unit (Ward/Commune/Town), District, Province
            if (entity.getPermanentAdministrativeUnit() != null) {
                AdministrativeUnit unit = entity.getPermanentAdministrativeUnit();
                this.permanentAdministrativeUnitId = unit.getId();
                this.permanentAdministrativeUnitName = unit.getName();
                if (unit.getParent() != null) {
                    AdministrativeUnit district = unit.getParent();
                    this.permanentDistrictName = district.getName();
                    if (district.getParent() != null) {
                        this.permanentProvinceName = district.getParent().getName();
                    }
                }
            }
            this.permanentAddressDetail = entity.getPermanentAddressDetail();

            // Current Administrative Unit (Ward/Commune/Town), District, Province
            if (entity.getCurrentAdministrativeUnit() != null) {
                AdministrativeUnit unit = entity.getCurrentAdministrativeUnit();
                this.currentAdministrativeUnitId = unit.getId();
                this.currentAdministrativeUnitName = unit.getName();
                if (unit.getParent() != null) {
                    AdministrativeUnit district = unit.getParent();
                    this.currentDistrictName = district.getName();
                    if (district.getParent() != null) {
                        this.currentProvinceName = district.getParent().getName();
                    }
                }
            }
            this.currentAddressDetail = entity.getCurrentAddressDetail();

            this.idNumberIssueDate = entity.getIdNumberIssueDate();
            this.idNumberIssueBy = entity.getIdNumberIssueBy();
            this.companyEmail = entity.getCompanyEmail();
            this.taxCode = entity.getTaxCode();
            this.healthInsuranceNumber = entity.getHealthInsuranceNumber();

            if (entity.getCertificates() != null && !entity.getCertificates().isEmpty()) {
                this.certificates = entity.getCertificates().stream()
                        .filter(c -> c.getIsDeleted() == null || !c.getIsDeleted())
                        .map(StaffCertificateDto::new)
                        .collect(Collectors.toList());
            }

            if (entity.getBankAccounts() != null && !entity.getBankAccounts().isEmpty()) {
                this.bankAccounts = entity.getBankAccounts().stream()
                        .filter(b -> b.getIsDeleted() == null || !b.getIsDeleted())
                        .map(StaffBankAccountDto::new)
                        .collect(Collectors.toList());

                StaffBankAccount defaultAcc = entity.getBankAccounts().stream()
                        .filter(b -> (b.getIsDeleted() == null || !b.getIsDeleted()) && Boolean.TRUE.equals(b.getIsDefault()))
                        .findFirst()
                        .orElse(entity.getBankAccounts().get(0));

                if (defaultAcc != null && defaultAcc.getBank() != null) {
                    this.bankId = defaultAcc.getBank().getId();
                    this.bankName = defaultAcc.getBank().getName();
                    this.bankBin = defaultAcc.getBank().getBin();
                    this.bankAccountNumber = defaultAcc.getAccountNumber();
                    this.bankAccountName = defaultAcc.getAccountName();
                }
            }
        }
    }
}
