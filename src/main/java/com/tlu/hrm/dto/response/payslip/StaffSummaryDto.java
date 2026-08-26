package com.tlu.hrm.dto.response.payslip;

import com.tlu.hrm.dto.request.StaffBankAccountDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffSummaryDto {
    private UUID id;
    private String staffCode;
    private String displayName;
    private UUID departmentId;
    private String departmentName;
    private UUID positionId;
    private String positionName;
    
    // Bank accounts array
    @Builder.Default
    private List<StaffBankAccountDto> bankAccounts = new ArrayList<>();
}
