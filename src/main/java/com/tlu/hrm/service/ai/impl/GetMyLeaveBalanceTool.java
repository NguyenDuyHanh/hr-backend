package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetMyLeaveBalanceTool implements AiTool {

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public String getName() {
        return "getMyLeaveBalance";
    }

    @Override
    public String getDescription() {
        return "Lấy thông tin số dư ngày nghỉ phép năm hiện tại. Người quản lý/admin có thể xem số dư phép của nhân viên khác bằng cách cung cấp tên hoặc mã nhân viên. Nhân viên bình thường chỉ xem được của chính mình.";
    }

    @Override
    public String getParametersJson() {
        return """
        {
          "type": "object",
          "properties": {
            "staffCode": {
              "type": "string",
              "description": "Mã số nhân viên cần tra cứu số dư phép (ví dụ: NV001)."
            },
            "name": {
              "type": "string",
              "description": "Tên nhân viên cần tra cứu số dư phép."
            }
          }
        }
        """;
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        String staffCode = arguments.has("staffCode") ? arguments.get("staffCode").asText() : null;
        String name = arguments.has("name") ? arguments.get("name").asText() : null;

        boolean isManagerOrAdmin = currentUser.getUserRoles().stream()
                .anyMatch(ur -> "ROLE_ADMIN".equals(ur.getRole().getName())
                        || "HR_MANAGER".equals(ur.getRole().getName()));

        Staff targetStaff = null;

        if (isManagerOrAdmin) {
            // Lấy theo tham số truyền vào
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

        // Nếu không là admin/manager hoặc không tìm thấy theo tham số, mặc định xem của chính mình
        if (targetStaff == null) {
            targetStaff = currentUser.getStaff();
        }

        if (targetStaff == null) {
            return "{\"status\": \"error\", \"message\": \"Không tìm thấy thông tin nhân sự để xem số dư phép.\"}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"status\": \"success\",");
        sb.append("\"staffCode\":\"").append(escapeJson(targetStaff.getStaffCode())).append("\",");
        sb.append("\"displayName\":\"").append(escapeJson(targetStaff.getDisplayName())).append("\",");
        sb.append("\"annualLeave\":").append(targetStaff.getAnnualLeave() != null ? targetStaff.getAnnualLeave() : 12.0);
        sb.append("}");
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
