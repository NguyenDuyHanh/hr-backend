package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.StaffSalaryItem;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.repository.StaffSalaryItemRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetStaffSalaryItemsTool implements AiTool {

    @Autowired
    private StaffSalaryItemRepository staffSalaryItemRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public String getName() {
        return "getStaffSalaryItems";
    }

    @Override
    public String getDescription() {
        return "Lấy thông tin chi tiết các mức lương cơ bản, phụ cấp hoặc các khoản khấu trừ cố định của nhân viên.";
    }

    @Override
    public String getParametersJson() {
        return """
        {
          "type": "object",
          "properties": {
            "staffCode": {
              "type": "string",
              "description": "Mã số nhân viên cần tra cứu (ví dụ: NV001)."
            },
            "name": {
              "type": "string",
              "description": "Tên nhân viên cần tra cứu."
            }
          }
        }
        """;
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        String staffCode = arguments.has("staffCode") ? arguments.get("staffCode").asText() : null;
        String name = arguments.has("name") ? arguments.get("name").asText() : null;

        boolean hasSalaryAccess = currentUser.getUserRoles().stream()
                .anyMatch(ur -> "ROLE_ADMIN".equals(ur.getRole().getName())
                        || "HR_MANAGER".equals(ur.getRole().getName())
                        || "HR_COMPENSATION_BENEFIT".equals(ur.getRole().getName()));

        Staff targetStaff = null;
        boolean hasParams = (staffCode != null && !staffCode.trim().isEmpty()) 
                || (name != null && !name.trim().isEmpty());

        if (hasSalaryAccess) {
            List<Staff> staffs = staffRepository.findAll();
            if (staffCode != null && !staffCode.trim().isEmpty()) {
                for (Staff s : staffs) {
                    if (s.getStaffCode() != null && matchStaffCode(s.getStaffCode(), staffCode)) {
                        targetStaff = s;
                        break;
                    }
                }
            } else if (name != null && !name.trim().isEmpty()) {
                for (Staff s : staffs) {
                    if (s.getDisplayName() != null && s.getDisplayName().toLowerCase().contains(name.toLowerCase().trim())) {
                        targetStaff = s;
                        break;
                    }
                }
            }
        }

        // Nếu không có quyền xem người khác hoặc không tìm thấy theo tham số, mặc định xem của chính mình nếu không truyền params
        if (targetStaff == null && !hasParams) {
            targetStaff = currentUser.getStaff();
        }

        if (targetStaff == null) {
            return "{\"status\": \"success\", \"message\": \"Không tìm thấy thông tin nhân sự phù hợp để xem mức lương.\" }";
        }

        List<StaffSalaryItem> salaryItems = staffSalaryItemRepository.findByStaffId(targetStaff.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"status\": \"success\",");
        sb.append("\"staffName\":\"").append(escapeJson(targetStaff.getDisplayName())).append("\",");
        sb.append("\"staffCode\":\"").append(escapeJson(targetStaff.getStaffCode())).append("\",");
        sb.append("\"count\": ").append(salaryItems.size()).append(",");
        sb.append("\"salaryItems\": [");

        for (int i = 0; i < salaryItems.size(); i++) {
            StaffSalaryItem ssi = salaryItems.get(i);
            if (ssi.getIsDeleted() != null && ssi.getIsDeleted()) {
                continue;
            }
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"name\":\"").append(ssi.getSalaryItem() != null ? escapeJson(ssi.getSalaryItem().getName()) : "").append("\",");
            sb.append("\"type\":\"").append(ssi.getSalaryItem() != null && ssi.getSalaryItem().getType() != null ? escapeJson(ssi.getSalaryItem().getType().name()) : "").append("\",");
            sb.append("\"amount\":").append(ssi.getAmount() != null ? ssi.getAmount() : 0.0).append(",");
            sb.append("\"calculationType\":\"").append(ssi.getSalaryItem() != null && ssi.getSalaryItem().getCalculationType() != null ? escapeJson(ssi.getSalaryItem().getCalculationType().name()) : "").append("\"");
            sb.append("}");
        }

        sb.append("]");
        sb.append("}");
        return sb.toString();
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
