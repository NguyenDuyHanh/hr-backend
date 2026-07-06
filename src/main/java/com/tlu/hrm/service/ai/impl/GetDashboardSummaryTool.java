package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.dto.response.DashboardSummaryResponse;
import com.tlu.hrm.model.User;
import com.tlu.hrm.service.DashboardService;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetDashboardSummaryTool implements AiTool {

    @Autowired
    private DashboardService dashboardService;

    @Override
    public String getName() {
        return "getDashboardSummary";
    }

    @Override
    public String getDescription() {
        return "Lấy thông tin tổng quan dashboard của toàn hệ thống (bao gồm tổng số nhân viên, dự án hoạt động, số đơn phép chờ duyệt, hoạt động gần đây, cơ cấu phòng ban...).";
    }

    @Override
    public String getParametersJson() {
        return "{\"type\":\"object\",\"properties\":{}}";
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        boolean hasAccess = currentUser.getUserRoles().stream()
                .anyMatch(ur -> "ROLE_ADMIN".equals(ur.getRole().getName())
                        || "HR_MANAGER".equals(ur.getRole().getName()));

        if (!hasAccess) {
            return "{\"status\": \"error\", \"message\": \"Bạn không có quyền thực hiện hành động này.\"}";
        }

        DashboardSummaryResponse summary = dashboardService.getSummary();
        if (summary == null) {
            return "{\"status\": \"error\", \"message\": \"Không thể tải thông tin tổng quan hệ thống.\"}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"status\": \"success\",");
        
        // KPI Summary
        if (summary.getKpiSummary() != null) {
            sb.append("\"totalStaff\":").append(summary.getKpiSummary().getTotalStaff()).append(",");
            sb.append("\"newStaffThisMonth\":").append(summary.getKpiSummary().getNewStaffThisMonth()).append(",");
            sb.append("\"pendingLeaveRequests\":").append(summary.getKpiSummary().getPendingLeaveRequests()).append(",");
            sb.append("\"activeProjects\":").append(summary.getKpiSummary().getActiveProjects()).append(",");
            sb.append("\"nearDeadlineProjects\":").append(summary.getKpiSummary().getNearDeadlineProjects()).append(",");
        }

        // Project Overview
        if (summary.getProjectOverview() != null) {
            sb.append("\"projectPlanning\":").append(summary.getProjectOverview().getPlanning()).append(",");
            sb.append("\"projectActive\":").append(summary.getProjectOverview().getActive()).append(",");
            sb.append("\"projectCompleted\":").append(summary.getProjectOverview().getCompleted()).append(",");
        }

        // Recruitment Pipeline
        if (summary.getRecruitmentPipeline() != null) {
            sb.append("\"candidateScreening\":").append(summary.getRecruitmentPipeline().getScreening()).append(",");
            sb.append("\"candidateInterview\":").append(summary.getRecruitmentPipeline().getInterview()).append(",");
            sb.append("\"candidateQualified\":").append(summary.getRecruitmentPipeline().getQualified()).append(",");
            sb.append("\"candidateOnboarded\":").append(summary.getRecruitmentPipeline().getOnboarded()).append(",");
        }

        // Department distribution
        sb.append("\"departments\": [");
        if (summary.getDepartmentDistribution() != null) {
            for (int i = 0; i < summary.getDepartmentDistribution().size(); i++) {
                var d = summary.getDepartmentDistribution().get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"name\":\"").append(escapeJson(d.getDepartmentName())).append("\",");
                sb.append("\"staffCount\":").append(d.getStaffCount()).append(",");
                sb.append("\"percentage\":").append(d.getPercentage());
                sb.append("}");
            }
        }
        sb.append("]");

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
