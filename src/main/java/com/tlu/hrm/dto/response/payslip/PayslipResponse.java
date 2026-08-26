package com.tlu.hrm.dto.response.payslip;

import com.tlu.hrm.enums.PaidStatus;
import com.tlu.hrm.model.Payslip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayslipResponse {
    private UUID id;

    // Staff summary
    private StaffSummaryDto staff;

    // Payroll summary (includes Period)
    private PayrollSummaryDto payroll;

    // Calculation metrics
    private Double totalWorkDays;
    private Double totalOtHours;
    private Double totalWeekendOtHours;
    private Double totalHolidayOtHours;
    private Double totalIncome;
    private Double totalDeduction;
    private Double netSalary;

    private PaidStatus paidStatus;
    private String note;

    private List<PayslipItemResponseDto> items;

    public static PayslipResponse fromEntity(Payslip entity) {
        if (entity == null) return null;

        PayslipResponseBuilder builder = PayslipResponse.builder()
                .id(entity.getId())
                .totalWorkDays(entity.getTotalWorkDays())
                .totalOtHours(entity.getTotalOtHours())
                .totalWeekendOtHours(entity.getTotalWeekendOtHours())
                .totalHolidayOtHours(entity.getTotalHolidayOtHours())
                .totalIncome(entity.getTotalIncome())
                .totalDeduction(entity.getTotalDeduction())
                .netSalary(entity.getNetSalary())
                .paidStatus(entity.getPaidStatus())
                .note(entity.getNote());

        if (entity.getPayroll() != null) {
            PeriodSummaryDto periodSummary = null;
            if (entity.getPayroll().getPeriod() != null) {
                periodSummary = PeriodSummaryDto.builder()
                        .id(entity.getPayroll().getPeriod().getId())
                        .name(entity.getPayroll().getPeriod().getName())
                        .month(entity.getPayroll().getPeriod().getMonth())
                        .year(entity.getPayroll().getPeriod().getYear())
                        .build();
            }

            builder.payroll(PayrollSummaryDto.builder()
                    .id(entity.getPayroll().getId())
                    .name(entity.getPayroll().getName())
                    .period(periodSummary)
                    .build());
        }

        if (entity.getStaff() != null) {
            var s = entity.getStaff();
            String deptName = s.getDepartment() != null ? s.getDepartment().getName() : null;
            UUID deptId = s.getDepartment() != null ? s.getDepartment().getId() : null;
            String posName = s.getPosition() != null ? s.getPosition().getName() : null;
            UUID posId = s.getPosition() != null ? s.getPosition().getId() : null;

            List<com.tlu.hrm.dto.request.StaffBankAccountDto> bankAccountDtoList = new ArrayList<>();

            if (s.getBankAccounts() != null && !s.getBankAccounts().isEmpty()) {
                for (var b : s.getBankAccounts()) {
                    if (b.getIsDeleted() != null && b.getIsDeleted()) continue;
                    bankAccountDtoList.add(new com.tlu.hrm.dto.request.StaffBankAccountDto(b));
                }
            }

            builder.staff(StaffSummaryDto.builder()
                    .id(s.getId())
                    .staffCode(s.getStaffCode())
                    .displayName(s.getDisplayName())
                    .departmentId(deptId)
                    .departmentName(deptName)
                    .positionId(posId)
                    .positionName(posName)
                    .bankAccounts(bankAccountDtoList)
                    .build());
        }

        if (entity.getItems() != null && !entity.getItems().isEmpty()) {
            builder.items(entity.getItems().stream().map(item -> {
                SalaryItemSummaryDto salaryItemSummary = null;
                if (item.getSalaryItem() != null) {
                    salaryItemSummary = SalaryItemSummaryDto.builder()
                            .id(item.getSalaryItem().getId())
                            .code(item.getSalaryItem().getCode())
                            .name(item.getSalaryItem().getName())
                            .type(item.getSalaryItem().getType() != null ? item.getSalaryItem().getType().name() : null)
                            .calculationType(item.getSalaryItem().getCalculationType() != null ? item.getSalaryItem().getCalculationType().name() : null)
                            .build();
                }

                return PayslipItemResponseDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .calculatedValue(item.getCalculatedValue())
                        .amount(item.getAmount())
                        .salaryItem(salaryItemSummary)
                        .build();
            }).collect(Collectors.toList()));
        } else {
            builder.items(new ArrayList<>());
        }

        return builder.build();
    }
}
