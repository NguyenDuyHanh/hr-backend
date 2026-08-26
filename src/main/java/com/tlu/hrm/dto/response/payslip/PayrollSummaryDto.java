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
public class PayrollSummaryDto {
    private UUID id;
    private String name;
    private PeriodSummaryDto period;
}
