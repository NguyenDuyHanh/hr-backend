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
        }
    }
}
