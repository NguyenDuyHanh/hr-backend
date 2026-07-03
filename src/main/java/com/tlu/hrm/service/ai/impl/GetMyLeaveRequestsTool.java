package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.LeaveRequest;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.LeaveRequestRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetMyLeaveRequestsTool implements AiTool {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Override
    public String getName() {
        return "getMyLeaveRequests";
    }

    @Override
    public String getDescription() {
        return "Lấy danh sách các đơn nghỉ phép (yêu cầu nghỉ phép) trong hệ thống. Người quản lý/admin sẽ xem được toàn bộ, nhân viên thông thường chỉ xem được các đơn của họ.";
    }

    @Override
    public String getParametersJson() {
        return "{\"type\":\"object\",\"properties\":{}}";
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        boolean isManagerOrAdmin = currentUser.getUserRoles().stream()
                .anyMatch(ur -> "ROLE_ADMIN".equals(ur.getRole().getName())
                        || "HR_MANAGER".equals(ur.getRole().getName()));

        List<LeaveRequest> all = leaveRequestRepository.findAll();
        List<LeaveRequest> filtered = new ArrayList<>();

        if (isManagerOrAdmin) {
            for (LeaveRequest r : all) {
                if (r.getIsDeleted() == null || !r.getIsDeleted()) {
                    filtered.add(r);
                }
            }
        } else {
            Staff staff = currentUser.getStaff();
            if (staff != null) {
                for (LeaveRequest r : all) {
                    if (r.getIsDeleted() == null || !r.getIsDeleted()) {
                        if (r.getRequestStaff() != null && r.getRequestStaff().getId().equals(staff.getId())) {
                            filtered.add(r);
                        }
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            return "{\"status\": \"success\", \"message\": \"Không tìm thấy yêu cầu nghỉ phép nào.\", \"requests\": []}";
        }

        // Sắp xếp đơn nghỉ phép mới nhất lên trước
        filtered.sort((a, b) -> {
            if (a.getCreateDate() == null || b.getCreateDate() == null) return 0;
            return b.getCreateDate().compareTo(a.getCreateDate());
        });

        // Giới hạn tối đa 30 đơn gần nhất để tránh tràn token
        int limit = Math.min(filtered.size(), 30);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\": \"success\", \"count\": ").append(limit).append(", \"requests\": [");
        for (int i = 0; i < limit; i++) {
            LeaveRequest r = filtered.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"id\":\"").append(r.getId()).append("\",");
            sb.append("\"staffName\":\"").append(r.getRequestStaff() != null ? escapeJson(r.getRequestStaff().getDisplayName()) : "—").append("\",");
            sb.append("\"staffCode\":\"").append(r.getRequestStaff() != null ? escapeJson(r.getRequestStaff().getStaffCode()) : "—").append("\",");
            sb.append("\"leaveType\":\"").append(r.getLeaveType() != null ? escapeJson(r.getLeaveType().name()) : "—").append("\",");
            sb.append("\"fromDate\":\"").append(r.getFromDate() != null ? r.getFromDate().toString() : "—").append("\",");
            sb.append("\"toDate\":\"").append(r.getToDate() != null ? r.getToDate().toString() : "—").append("\",");
            sb.append("\"totalDays\":").append(r.getTotalDays() != null ? r.getTotalDays() : 0).append(",");
            sb.append("\"approvalStatus\":\"").append(r.getApprovalStatus() != null ? escapeJson(r.getApprovalStatus().name()) : "—").append("\",");
            sb.append("\"requestReason\":\"").append(r.getRequestReason() != null ? escapeJson(r.getRequestReason()) : "").append("\",");
            sb.append("\"rejectReason\":\"").append(r.getRejectReason() != null ? escapeJson(r.getRejectReason()) : "").append("\"");
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
