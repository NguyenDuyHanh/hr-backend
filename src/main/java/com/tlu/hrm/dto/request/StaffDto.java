package com.tlu.hrm.dto.request;

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
}
