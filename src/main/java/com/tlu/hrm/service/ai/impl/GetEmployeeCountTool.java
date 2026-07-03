package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetEmployeeCountTool implements AiTool {

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public String getName() {
        return "getEmployeeCount";
    }

    @Override
    public String getDescription() {
        return "Lấy tổng số lượng nhân viên/nhân sự hiện có trong toàn hệ thống quản lý.";
    }

    @Override
    public String getParametersJson() {
        return "{\"type\":\"object\",\"properties\":{}}";
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        // RBAC: Chỉ ADMIN và HR_MANAGER được xem thống kê số lượng nhân viên
        boolean isManagerOrAdmin = currentUser.getUserRoles().stream()
                .anyMatch(ur -> "ROLE_ADMIN".equals(ur.getRole().getName())
                        || "HR_MANAGER".equals(ur.getRole().getName()));

        if (!isManagerOrAdmin) {
            return "{\"status\": \"error\", \"message\": \"Bạn không có quyền thực hiện hành động này.\"}";
        }

        long count = staffRepository.count();
        return "{\"status\": \"success\", \"totalEmployees\": " + count + "}";
    }
}
