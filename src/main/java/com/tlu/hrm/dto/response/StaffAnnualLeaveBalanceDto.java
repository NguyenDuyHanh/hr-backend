package com.tlu.hrm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAnnualLeaveBalanceDto {
    private UUID staffId;
    private String staffName;
    private String staffCode;
    private String departmentName;
    private String positionName;
    private Integer year;
    private Double annualLeaveLimit;
    private Double usedDays;
    private Double remainingDays;
}
