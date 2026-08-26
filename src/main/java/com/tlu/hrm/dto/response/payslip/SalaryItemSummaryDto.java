package com.tlu.hrm.dto.response.payslip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryItemSummaryDto {
    private UUID id;
    private String code;
    private String name;
    private String type;
    private String calculationType;
}
