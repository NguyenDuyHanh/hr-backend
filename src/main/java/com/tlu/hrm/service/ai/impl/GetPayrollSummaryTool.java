package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Payroll;
import com.tlu.hrm.model.Payslip;
import com.tlu.hrm.model.User;
import com.tlu.hrm.enums.PaidStatus;
import com.tlu.hrm.repository.PayrollRepository;
import com.tlu.hrm.repository.PayslipRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetPayrollSummaryTool implements AiTool {

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private PayslipRepository payslipRepository;

    @Override
    public String getName() {
        return "getPayrollSummary";
    }

    @Override
    public String getDescription() {
        return "Lấy thông tin tổng hợp về các bảng lương trong hệ thống (tổng quỹ lương, số phiếu lương đã thanh toán và chưa thanh toán).";
    }

    @Override
    public String getParametersJson() {
        return "{\"type\":\"object\",\"properties\":{}}";
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        boolean hasPayrollAccess = currentUser.getUserRoles().stream()
                .anyMatch(ur -> "ROLE_ADMIN".equals(ur.getRole().getName())
                        || "HR_MANAGER".equals(ur.getRole().getName())
                        || "HR_COMPENSATION_BENEFIT".equals(ur.getRole().getName()));

        if (!hasPayrollAccess) {
            return "{\"status\": \"error\", \"message\": \"Bạn không có quyền thực hiện hành động này.\"}";
        }

        List<Payroll> payrolls = payrollRepository.findAll();
        long activePayrollCount = payrolls.stream()
                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                .count();

        List<Payslip> payslips = payslipRepository.findAll();
        double totalNetSalary = 0;
        double totalIncome = 0;
        double totalDeduction = 0;
        long paidCount = 0;
        long unpaidCount = 0;

        for (Payslip p : payslips) {
            if (p.getIsDeleted() != null && p.getIsDeleted()) {
                continue;
            }
            if (p.getNetSalary() != null) {
                totalNetSalary += p.getNetSalary();
            }
            if (p.getTotalIncome() != null) {
                totalIncome += p.getTotalIncome();
            }
            if (p.getTotalDeduction() != null) {
                totalDeduction += p.getTotalDeduction();
            }
            if (p.getPaidStatus() == PaidStatus.PAID) {
                paidCount++;
            } else {
                unpaidCount++;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"status\": \"success\",");
        sb.append("\"totalPayrolls\": ").append(activePayrollCount).append(",");
        sb.append("\"totalPayslips\": ").append(paidCount + unpaidCount).append(",");
        sb.append("\"paidCount\": ").append(paidCount).append(",");
        sb.append("\"unpaidCount\": ").append(unpaidCount).append(",");
        sb.append("\"totalNetSalary\": ").append(totalNetSalary).append(",");
        sb.append("\"totalIncome\": ").append(totalIncome).append(",");
        sb.append("\"totalDeduction\": ").append(totalDeduction);
        sb.append("}");
        return sb.toString();
    }
}
