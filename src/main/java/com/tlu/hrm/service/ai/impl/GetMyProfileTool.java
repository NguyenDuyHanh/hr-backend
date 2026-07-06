package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.User;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.stereotype.Component;

@Component
public class GetMyProfileTool implements AiTool {

    @Override
    public String getName() {
        return "getMyProfile";
    }

    @Override
    public String getDescription() {
        return "Lấy thông tin hồ sơ chi tiết của chính bản thân người dùng đang đăng nhập (bao gồm họ tên, mã nhân viên, phòng ban, chức danh/vị trí, email, số điện thoại, ngày vào làm). Sử dụng khi người dùng hỏi các câu hỏi như: tôi là ai, tôi thuộc phòng ban nào, xem hồ sơ của tôi, mã nhân viên của tôi là gì.";
    }

    @Override
    public String getParametersJson() {
        return "{ \"type\": \"object\", \"properties\": {} }";
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        Staff myStaff = currentUser.getStaff();
        if (myStaff == null) {
            return "{\"status\": \"error\", \"message\": \"Tài khoản của bạn chưa được liên kết với hồ sơ nhân viên nào trong hệ thống.\"}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"staffCode\":\"").append(escapeJson(myStaff.getStaffCode())).append("\",");
        sb.append("\"displayName\":\"").append(escapeJson(myStaff.getDisplayName())).append("\",");
        sb.append("\"email\":\"").append(escapeJson(myStaff.getEmail())).append("\",");
        sb.append("\"phoneNumber\":\"").append(escapeJson(myStaff.getPhoneNumber())).append("\",");
        sb.append("\"gender\":\"").append(myStaff.getGender() != null ? escapeJson(myStaff.getGender().name()) : "").append("\",");
        sb.append("\"workingStatus\":\"").append(myStaff.getWorkingStatus() != null ? escapeJson(myStaff.getWorkingStatus().name()) : "").append("\",");
        sb.append("\"birthDate\":\"").append(myStaff.getBirthDate() != null ? myStaff.getBirthDate().toString() : "—").append("\",");
        sb.append("\"startDate\":\"").append(myStaff.getStartDate() != null ? myStaff.getStartDate().toString() : "—").append("\",");
        sb.append("\"currentAddress\":\"").append(myStaff.getCurrentAddress() != null ? escapeJson(myStaff.getCurrentAddress()) : "—").append("\",");
        sb.append("\"department\":\"").append(myStaff.getDepartment() != null ? escapeJson(myStaff.getDepartment().getName()) : "—").append("\",");
        sb.append("\"position\":\"").append(myStaff.getPosition() != null ? escapeJson(myStaff.getPosition().getName()) : "—").append("\"");
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
