package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.response.DashboardSummaryResponse;
import com.tlu.hrm.enums.CandidateStatus;
import com.tlu.hrm.enums.LeaveApprovalStatus;
import com.tlu.hrm.enums.TimesheetStatus;
import com.tlu.hrm.enums.WorkingStatus;
import com.tlu.hrm.model.Department;
import com.tlu.hrm.model.LeaveRequest;
import com.tlu.hrm.model.Project;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.repository.*;
import com.tlu.hrm.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

        @Autowired
        private StaffRepository staffRepository;

        @Autowired
        private LeaveRequestRepository leaveRequestRepository;

        @Autowired
        private ProjectRepository projectRepository;

        @Autowired
        private CandidateRepository candidateRepository;

        @Autowired
        private DepartmentRepository departmentRepository;

        @Autowired
        private TimesheetRepository timesheetRepository;

        @Override
        public DashboardSummaryResponse getSummary() {
                final LocalDate todayVal = LocalDate.now();

                // 1. KPI Summary
                long totalStaff = staffRepository.countAllActiveStaff();

                long pendingTimesheetsToday = timesheetRepository.countByWorkingDateAndStatusAndNotDeleted(todayVal,
                                TimesheetStatus.SUBMITTED);
                long pendingTimesheetsYesterday = timesheetRepository.countByWorkingDateAndStatusAndNotDeleted(
                                todayVal.minusDays(1), TimesheetStatus.SUBMITTED);

                LocalDate startOfThisMonth = todayVal.withDayOfMonth(1);
                long newStaffThisMonth = staffRepository.countNewStaffBetween(startOfThisMonth, todayVal);

                LocalDate startOfLastMonth = todayVal.minusMonths(1).withDayOfMonth(1);
                LocalDate endOfLastMonth = todayVal.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                long newStaffLastMonth = staffRepository.countNewStaffBetween(startOfLastMonth, endOfLastMonth);

                long pendingLeavesCount = leaveRequestRepository
                                .countByApprovalStatusAndNotDeleted(LeaveApprovalStatus.PENDING);
                long activeProjectsCount = projectRepository.countByIsFinishedAndNotDeleted(false);
                long nearDeadlineProjectsCount = projectRepository.countNearDeadlineProjects(todayVal.plusDays(7));

                // 6-month sparkline of new hires
                List<Long> sparkline = new ArrayList<>();
                for (int i = 5; i >= 0; i--) {
                        LocalDate start = todayVal.minusMonths(i).withDayOfMonth(1);
                        LocalDate end = todayVal.minusMonths(i).with(TemporalAdjusters.lastDayOfMonth());
                        sparkline.add(staffRepository.countNewStaffBetween(start, end));
                }

                DashboardSummaryResponse.KpiSummary kpiSummary = DashboardSummaryResponse.KpiSummary.builder()
                                .totalStaff(totalStaff)
                                .newStaffThisMonth(newStaffThisMonth)
                                .newStaffLastMonth(newStaffLastMonth)
                                .pendingTimesheetsToday(pendingTimesheetsToday)
                                .pendingTimesheetsYesterday(pendingTimesheetsYesterday)
                                .pendingLeaveRequests(pendingLeavesCount)
                                .activeProjects(activeProjectsCount)
                                .nearDeadlineProjects(nearDeadlineProjectsCount)
                                .staffTrendSpark(sparkline)
                                .build();

                // 2. Staff Trend (12 Months)
                List<DashboardSummaryResponse.MonthlyStaffTrend> staffTrend = new ArrayList<>();
                for (int i = 11; i >= 0; i--) {
                        LocalDate start = todayVal.minusMonths(i).withDayOfMonth(1);
                        LocalDate end = todayVal.minusMonths(i).with(TemporalAdjusters.lastDayOfMonth());
                        long newHires = staffRepository.countNewStaffBetween(start, end);

                        // Resignations calculation: count active/deleted staff who had workingStatus =
                        // "5" (Resigned) in that month
                        long resignations = staffRepository.findAll().stream()
                                        .filter(s -> WorkingStatus.RESIGNED == s.getWorkingStatus())
                                        .filter(s -> {
                                                LocalDate changeDate = s.getModifyDate() != null
                                                                ? s.getModifyDate().toLocalDate()
                                                                : s.getCreateDate() != null
                                                                                ? s.getCreateDate().toLocalDate()
                                                                                : null;
                                                return changeDate != null && !changeDate.isBefore(start)
                                                                && !changeDate.isAfter(end);
                                        })
                                        .count();

                        staffTrend.add(DashboardSummaryResponse.MonthlyStaffTrend.builder()
                                        .month("T" + start.getMonthValue() + "/" + (start.getYear() % 100))
                                        .newHires(newHires)
                                        .resignations(resignations)
                                        .build());
                }

                // 3. Department Distribution
                List<Department> departments = departmentRepository.findAll();
                List<Staff> activeStaff = staffRepository.findActiveStaffs();
                long totalActive = activeStaff.size();

                List<DashboardSummaryResponse.DepartmentDistribution> departmentDistribution = new ArrayList<>();
                for (Department dept : departments) {
                        long count = activeStaff.stream()
                                        .filter(s -> s.getDepartment() != null
                                                        && s.getDepartment().getId().equals(dept.getId()))
                                        .count();
                        if (count > 0) {
                                double pct = totalActive > 0 ? (double) count / totalActive * 100.0 : 0.0;
                                pct = Math.round(pct * 10.0) / 10.0;
                                departmentDistribution.add(DashboardSummaryResponse.DepartmentDistribution.builder()
                                                .departmentId(dept.getId())
                                                .departmentName(dept.getName())
                                                .staffCount(count)
                                                .percentage(pct)
                                                .build());
                        }
                }
                // Sort descending by staffCount
                departmentDistribution.sort((d1, d2) -> Long.compare(d2.getStaffCount(), d1.getStaffCount()));

                // Group "Khác" if > 5 departments
                if (departmentDistribution.size() > 5) {
                        List<DashboardSummaryResponse.DepartmentDistribution> top5 = new ArrayList<>(
                                        departmentDistribution.subList(0, 5));
                        long otherCount = 0;
                        double otherPct = 0.0;
                        for (int i = 5; i < departmentDistribution.size(); i++) {
                                otherCount += departmentDistribution.get(i).getStaffCount();
                                otherPct += departmentDistribution.get(i).getPercentage();
                        }
                        top5.add(DashboardSummaryResponse.DepartmentDistribution.builder()
                                        .departmentId(null)
                                        .departmentName("Khác")
                                        .staffCount(otherCount)
                                        .percentage(Math.round(otherPct * 10.0) / 10.0)
                                        .build());
                        departmentDistribution = top5;
                }

                // 4. Recent Activities (Aggregate from multiple tables)
                List<DashboardSummaryResponse.RecentActivityItem> recentActivities = new ArrayList<>();

                // Add recent staff additions
                List<Staff> allStaff = staffRepository.findActiveStaffs();
                allStaff.sort((s1, s2) -> {
                        LocalDateTime t1 = s1.getCreateDate() != null ? s1.getCreateDate() : LocalDateTime.now();
                        LocalDateTime t2 = s2.getCreateDate() != null ? s2.getCreateDate() : LocalDateTime.now();
                        return t2.compareTo(t1);
                });
                allStaff.stream().limit(5).forEach(s -> {
                        LocalDateTime ts = s.getCreateDate() != null ? s.getCreateDate()
                                        : (s.getStartDate() != null ? s.getStartDate().atStartOfDay()
                                                        : LocalDateTime.now());
                        recentActivities.add(DashboardSummaryResponse.RecentActivityItem.builder()
                                        .type("STAFF")
                                        .title("Nhân sự mới")
                                        .description("Nhân viên " + s.getDisplayName() + " (" + s.getStaffCode()
                                                        + ") đã được thêm vào hệ thống.")
                                        .staffName(s.getDisplayName())
                                        .staffAvatar(s.getAvatarUrl())
                                        .timestamp(ts)
                                        .build());
                });

                // Add recent leave requests
                List<LeaveRequest> recentLeavesList = leaveRequestRepository.findRecentLeaves(PageRequest.of(0, 5));
                recentLeavesList.forEach(l -> {
                        LocalDateTime ts = l.getCreateDate() != null ? l.getCreateDate() : LocalDateTime.now();
                        String statusText = l.getApprovalStatus() == LeaveApprovalStatus.PENDING ? "Đang chờ duyệt"
                                        : l.getApprovalStatus() == LeaveApprovalStatus.APPROVED ? "Đã duyệt"
                                                        : "Bị từ chối";
                        recentActivities.add(DashboardSummaryResponse.RecentActivityItem.builder()
                                        .type("LEAVE")
                                        .title("Yêu cầu nghỉ phép")
                                        .description(l.getRequestStaff().getDisplayName() + " đăng ký nghỉ "
                                                        + l.getLeaveType() + " (" + l.getTotalDays() + " ngày) - "
                                                        + statusText)
                                        .staffName(l.getRequestStaff().getDisplayName())
                                        .staffAvatar(l.getRequestStaff().getAvatarUrl())
                                        .timestamp(ts)
                                        .build());
                });

                // Add recent projects
                List<Project> allProjects = projectRepository.findAll();
                allProjects.removeIf(p -> p.getIsDeleted() != null && p.getIsDeleted());
                allProjects.sort((p1, p2) -> {
                        LocalDateTime t1 = p1.getCreateDate() != null ? p1.getCreateDate() : LocalDateTime.now();
                        LocalDateTime t2 = p2.getCreateDate() != null ? p2.getCreateDate() : LocalDateTime.now();
                        return t2.compareTo(t1);
                });
                allProjects.stream().limit(5).forEach(p -> {
                        LocalDateTime ts = p.getCreateDate() != null ? p.getCreateDate() : LocalDateTime.now();
                        recentActivities.add(DashboardSummaryResponse.RecentActivityItem.builder()
                                        .type("PROJECT")
                                        .title("Dự án mới")
                                        .description("Dự án '" + p.getName() + "' (" + p.getCode() + ") đã khởi tạo.")
                                        .staffName("Hệ thống")
                                        .staffAvatar(null)
                                        .timestamp(ts)
                                        .build());
                });

                // Sort recentActivities combined by timestamp DESC
                recentActivities.sort((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()));
                List<DashboardSummaryResponse.RecentActivityItem> limitedActivities = recentActivities.stream()
                                .limit(8)
                                .collect(Collectors.toList());

                // 5. Pending Leaves List
                List<LeaveRequest> pendingLeavesList = leaveRequestRepository.findRecentLeaves(PageRequest.of(0, 15));
                pendingLeavesList.removeIf(l -> l.getApprovalStatus() != LeaveApprovalStatus.PENDING);
                List<DashboardSummaryResponse.PendingLeaveItem> pendingLeaves = pendingLeavesList.stream()
                                .limit(5)
                                .map(l -> DashboardSummaryResponse.PendingLeaveItem.builder()
                                                .leaveId(l.getId())
                                                .staffName(l.getRequestStaff().getDisplayName())
                                                .staffAvatar(l.getRequestStaff().getAvatarUrl())
                                                .leaveType(l.getLeaveType().name())
                                                .fromDate(l.getFromDate())
                                                .toDate(l.getToDate())
                                                .totalDays(l.getTotalDays())
                                                .requestDate(l.getRequestDate())
                                                .build())
                                .collect(Collectors.toList());

                // 6. Project Overview
                List<Project> activeProjectsList = projectRepository.findAll().stream()
                                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                                .collect(Collectors.toList());

                long completedProj = activeProjectsList.stream()
                                .filter(p -> p.getIsFinished() != null && p.getIsFinished()).count();
                long activeProj = activeProjectsList.stream()
                                .filter(p -> (p.getIsFinished() == null || !p.getIsFinished()) &&
                                                p.getStartDate() != null && !p.getStartDate().isAfter(todayVal))
                                .count();
                long planningProj = activeProjectsList.stream()
                                .filter(p -> (p.getIsFinished() == null || !p.getIsFinished()) &&
                                                (p.getStartDate() == null || p.getStartDate().isAfter(todayVal)))
                                .count();

                DashboardSummaryResponse.ProjectOverview projectOverview = DashboardSummaryResponse.ProjectOverview
                                .builder()
                                .planning(planningProj)
                                .active(activeProj)
                                .completed(completedProj)
                                .build();

                // 7. Recruitment Pipeline
                long screening = candidateRepository.countByStatusAndNotDeleted(CandidateStatus.SCREENING);
                long interview = candidateRepository.countByStatusAndNotDeleted(CandidateStatus.INTERVIEW);
                long qualified = candidateRepository.countByStatusAndNotDeleted(CandidateStatus.QUALIFIED);
                long waiting = candidateRepository.countByStatusAndNotDeleted(CandidateStatus.WAITING);
                long onboarded = candidateRepository.countByStatusAndNotDeleted(CandidateStatus.ONBOARDED);

                DashboardSummaryResponse.RecruitmentPipeline recruitmentPipeline = DashboardSummaryResponse.RecruitmentPipeline
                                .builder()
                                .screening(screening)
                                .interview(interview)
                                .qualified(qualified)
                                .waiting(waiting)
                                .onboarded(onboarded)
                                .build();

                return DashboardSummaryResponse.builder()
                                .kpiSummary(kpiSummary)
                                .staffTrend(staffTrend)
                                .departmentDistribution(departmentDistribution)
                                .recentActivities(limitedActivities)
                                .pendingLeaves(pendingLeaves)
                                .projectOverview(projectOverview)
                                .recruitmentPipeline(recruitmentPipeline)
                                .build();
        }
}
