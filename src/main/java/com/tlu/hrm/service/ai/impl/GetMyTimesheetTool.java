package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.Timesheet;
import com.tlu.hrm.model.TimesheetDetail;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.TimesheetRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class GetMyTimesheetTool implements AiTool {

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Override
    public String getName() {
        return "getMyTimesheet";
    }

    @Override
    public String getDescription() {
        return "Lấy thông tin chấm công, số buổi/ngày đi làm thực tế và giờ tăng ca của bản thân trong một tháng cụ thể.";
    }

    @Override
    public String getParametersJson() {
        return """
        {
          "type": "object",
          "properties": {
            "month": {
              "type": "integer",
              "description": "Tháng cần tra cứu (từ 1 đến 12). Nếu không truyền, mặc định là tháng hiện tại."
            },
            "year": {
              "type": "integer",
              "description": "Năm cần tra cứu (ví dụ: 2026). Nếu không truyền, mặc định là năm hiện tại."
            }
          }
        }
        """;
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        Staff myStaff = currentUser.getStaff();
        if (myStaff == null) {
            return "{\"status\": \"error\", \"message\": \"Tài khoản của bạn chưa được liên kết với hồ sơ nhân viên nào trong hệ thống.\"}";
        }

        LocalDate today = LocalDate.now();
        int month = arguments.has("month") && !arguments.get("month").isNull() ? arguments.get("month").asInt() : today.getMonthValue();
        int year = arguments.has("year") && !arguments.get("year").isNull() ? arguments.get("year").asInt() : today.getYear();

        if (month < 1 || month > 12) {
            return "{\"status\": \"error\", \"message\": \"Tháng không hợp lệ.\"}";
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Timesheet> timesheets = timesheetRepository.findByStaffIdAndWorkingDateBetweenOrderByWorkingDateAsc(
                myStaff.getId(), start, end);

        int totalDaysCheckedIn = 0;
        double actualWorkingDays = 0.0;
        double overtimeHours = 0.0;
        double weekendOvertimeHours = 0.0;
        double holidayOvertimeHours = 0.0;
        int totalLateMinutes = 0;
        int totalEarlyMinutes = 0;

        for (Timesheet t : timesheets) {
            if (t.getIsDeleted() != null && t.getIsDeleted()) {
                continue;
            }
            // Một ngày đi làm là ngày có check-in và có ghi nhận tỷ lệ công việc > 0
            if (t.getTotalWorkRatio() != null && t.getTotalWorkRatio() > 0.0) {
                totalDaysCheckedIn++;
                actualWorkingDays += t.getTotalWorkRatio();
            }
            if (t.getOvertimeHours() != null) {
                overtimeHours += t.getOvertimeHours();
            }
            if (t.getWeekendOvertimeHours() != null) {
                weekendOvertimeHours += t.getWeekendOvertimeHours();
            }
            if (t.getHolidayOvertimeHours() != null) {
                holidayOvertimeHours += t.getHolidayOvertimeHours();
            }
            
            // Tính số phút đi muộn, về sớm từ chi tiết ca làm việc
            if (t.getDetails() != null) {
                for (TimesheetDetail td : t.getDetails()) {
                    if (td.getIsDeleted() != null && td.getIsDeleted()) {
                        continue;
                    }
                    if (td.getLateMinutes() != null) {
                        totalLateMinutes += td.getLateMinutes();
                    }
                    if (td.getEarlyMinutes() != null) {
                        totalEarlyMinutes += td.getEarlyMinutes();
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"status\":\"success\",");
        sb.append("\"month\":").append(month).append(",");
        sb.append("\"year\":").append(year).append(",");
        sb.append("\"totalDaysCheckedIn\":").append(totalDaysCheckedIn).append(",");
        sb.append("\"actualWorkingDays\":").append(actualWorkingDays).append(",");
        sb.append("\"overtimeHours\":").append(overtimeHours).append(",");
        sb.append("\"weekendOvertimeHours\":").append(weekendOvertimeHours).append(",");
        sb.append("\"holidayOvertimeHours\":").append(holidayOvertimeHours).append(",");
        sb.append("\"totalLateMinutes\":").append(totalLateMinutes).append(",");
        sb.append("\"totalEarlyMinutes\":").append(totalEarlyMinutes);
        sb.append("}");
        return sb.toString();
    }
}
