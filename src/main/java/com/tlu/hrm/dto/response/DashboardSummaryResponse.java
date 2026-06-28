package com.tlu.hrm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    
    private KpiSummary kpiSummary;
    private List<MonthlyStaffTrend> staffTrend;
    private List<DepartmentDistribution> departmentDistribution;
    private List<RecentActivityItem> recentActivities;
    private List<PendingLeaveItem> pendingLeaves;
    private ProjectOverview projectOverview;
    private RecruitmentPipeline recruitmentPipeline;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiSummary {
        private long totalStaff;
        private long newStaffThisMonth;
        private long newStaffLastMonth;
        private long pendingTimesheetsToday;
        private long pendingTimesheetsYesterday;
        private long pendingLeaveRequests;
        private long activeProjects;
        private long nearDeadlineProjects;
        private List<Long> staffTrendSpark;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStaffTrend {
        private String month;
        private long newHires;
        private long resignations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentDistribution {
        private UUID departmentId;
        private String departmentName;
        private long staffCount;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivityItem {
        private String type; // LEAVE, PROJECT, TASK, STAFF, RECRUITMENT
        private String title;
        private String description;
        private String staffName;
        private String staffAvatar;
        private LocalDateTime timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingLeaveItem {
        private UUID leaveId;
        private String staffName;
        private String staffAvatar;
        private String leaveType;
        private LocalDate fromDate;
        private LocalDate toDate;
        private double totalDays;
        private LocalDate requestDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectOverview {
        private long planning;
        private long active;
        private long completed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecruitmentPipeline {
        private long screening;
        private long interview;
        private long qualified;
        private long waiting;
        private long onboarded;
    }
}
