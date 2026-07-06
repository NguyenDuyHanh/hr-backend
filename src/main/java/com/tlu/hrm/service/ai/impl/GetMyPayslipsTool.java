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
        boolean hasParams = (staffCode != null && !staffCode.trim().isEmpty()) 
                || (name != null && !name.trim().isEmpty());

        if (hasPayrollAccess) {
            if (staffCode != null && !staffCode.trim().isEmpty()) {
                List<Staff> staffs = staffRepository.findAll();
                for (Staff s : staffs) {
                    if (s.getStaffCode() != null && matchStaffCode(s.getStaffCode(), staffCode)) {
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

        if (targetStaff == null && !hasParams) {
            targetStaff = currentUser.getStaff();
        }

        if (targetStaff == null) {
            return "{\"status\": \"success\", \"message\": \"Không tìm thấy thông tin nhân sự phù hợp để truy vấn phiếu lương.\"}";
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

        // Sắp xếp phiếu lương theo năm và tháng giảm dần
        filtered.sort((a, b) -> {
            int yearA = getPayslipYear(a);
            int yearB = getPayslipYear(b);
            if (yearA != yearB) {
                return Integer.compare(yearB, yearA);
            }
            return Integer.compare(getPayslipMonth(b), getPayslipMonth(a));
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
            sb.append("\"month\":").append(getPayslipMonth(p)).append(",");
            sb.append("\"year\":").append(getPayslipYear(p)).append(",");
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

    private int getPayslipYear(Payslip p) {
        if (p.getPayroll() != null && p.getPayroll().getPeriod() != null) {
            return p.getPayroll().getPeriod().getYear() != null ? p.getPayroll().getPeriod().getYear() : 0;
        }
        return 0;
    }

    private int getPayslipMonth(Payslip p) {
        if (p.getPayroll() != null && p.getPayroll().getPeriod() != null) {
            return p.getPayroll().getPeriod().getMonth() != null ? p.getPayroll().getPeriod().getMonth() : 0;
        }
        return 0;
    }

    private boolean matchStaffCode(String dbCode, String inputCode) {
        if (dbCode == null || inputCode == null) return false;
        String cleanDb = cleanCode(dbCode);
        String cleanInput = cleanCode(inputCode);
        return cleanDb.equalsIgnoreCase(cleanInput);
    }

    private String cleanCode(String code) {
        if (code == null) return "";
        String result = code.trim().toUpperCase();
        while (result.startsWith("NV") || result.startsWith("MÃ") || result.startsWith("MA")) {
            if (result.startsWith("NV")) {
                result = result.substring(2).trim();
            } else if (result.startsWith("MÃ")) {
                result = result.substring(2).trim();
            } else if (result.startsWith("MA")) {
                result = result.substring(2).trim();
            }
        }
        return result;
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
