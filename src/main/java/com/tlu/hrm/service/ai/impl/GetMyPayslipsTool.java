package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Payslip;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.PayslipRepository;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetMyPayslipsTool implements AiTool {

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public String getName() {
        return "getMyPayslips";
    }

    @Override
    public String getDescription() {
        return "Lấy danh sách phiếu lương (payslips) và thông tin thực nhận lương. Người quản lý lương/admin có thể xem của nhân viên khác. Nhân viên bình thường chỉ xem được của họ.";
    }

    @Override
    public String getParametersJson() {
        return """
        {
          "type": "object",
          "properties": {
            "staffCode": {
              "type": "string",
              "description": "Mã số nhân viên muốn tra cứu phiếu lương (ví dụ: NV001)."
            },
            "name": {
              "type": "string",
              "description": "Tên nhân viên muốn tra cứu phiếu lương."
            }
          }
        }
        """;
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        String staffCode = arguments.has("staffCode") ? arguments.get("staffCode").asText() : null;
        String name = arguments.has("name") ? arguments.get("name").asText() : null;

        boolean hasPayrollAccess = currentUser.getUserRoles().stream()
                .anyMatch(ur -> "ROLE_ADMIN".equals(ur.getRole().getName())
                        || "HR_MANAGER".equals(ur.getRole().getName())
                        || "HR_COMPENSATION_BENEFIT".equals(ur.getRole().getName()));

        Staff targetStaff = null;

        if (hasPayrollAccess) {
            if (staffCode != null && !staffCode.trim().isEmpty()) {
                List<Staff> staffs = staffRepository.findAll();
                for (Staff s : staffs) {
                    if (s.getStaffCode() != null && s.getStaffCode().trim().equalsIgnoreCase(staffCode.trim())) {
                        targetStaff = s;
                        break;
                    }
                }
            } else if (name != null && !name.trim().isEmpty()) {
                List<Staff> staffs = staffRepository.findAll();
                for (Staff s : staffs) {
                    if (s.getDisplayName() != null && s.getDisplayName().toLowerCase().contains(name.toLowerCase().trim())) {
                        targetStaff = s;
                        break;
                    }
                }
            }
        }

        if (targetStaff == null) {
            targetStaff = currentUser.getStaff();
        }

        if (targetStaff == null) {
            return "{\"status\": \"error\", \"message\": \"Không tìm thấy thông tin nhân sự để truy vấn phiếu lương.\"}";
        }

        List<Payslip> all = payslipRepository.findAll();
        List<Payslip> filtered = new ArrayList<>();

        for (Payslip p : all) {
            if (p.getIsDeleted() == null || !p.getIsDeleted()) {
                if (p.getStaff() != null && p.getStaff().getId().equals(targetStaff.getId())) {
                    filtered.add(p);
                }
            }
        }

        if (filtered.isEmpty()) {
            return "{\"status\": \"success\", \"message\": \"Không tìm thấy phiếu lương nào cho nhân viên này.\", \"payslips\": []}";
        }

        // Sắp xếp phiếu lương mới nhất lên trước
        filtered.sort((a, b) -> {
            if (a.getCreateDate() == null || b.getCreateDate() == null) return 0;
            return b.getCreateDate().compareTo(a.getCreateDate());
        });

        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\": \"success\", \"staffName\":\"").append(escapeJson(targetStaff.getDisplayName())).append("\",");
        sb.append("\"staffCode\":\"").append(escapeJson(targetStaff.getStaffCode())).append("\",");
        sb.append("\"count\": ").append(filtered.size()).append(", \"payslips\": [");
        
        for (int i = 0; i < filtered.size(); i++) {
            Payslip p = filtered.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"payrollName\":\"").append(p.getPayroll() != null ? escapeJson(p.getPayroll().getName()) : "—").append("\",");
            sb.append("\"totalWorkDays\":").append(p.getTotalWorkDays() != null ? p.getTotalWorkDays() : 0.0).append(",");
            sb.append("\"totalOtHours\":").append(p.getTotalOtHours() != null ? p.getTotalOtHours() : 0.0).append(",");
            sb.append("\"totalIncome\":").append(p.getTotalIncome() != null ? p.getTotalIncome() : 0.0).append(",");
            sb.append("\"totalDeduction\":").append(p.getTotalDeduction() != null ? p.getTotalDeduction() : 0.0).append(",");
            sb.append("\"netSalary\":").append(p.getNetSalary() != null ? p.getNetSalary() : 0.0).append(",");
            sb.append("\"paidStatus\":\"").append(p.getPaidStatus() != null ? escapeJson(p.getPaidStatus().name()) : "UNPAID").append("\",");
            sb.append("\"note\":\"").append(p.getNote() != null ? escapeJson(p.getNote()) : "").append("\"");
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
