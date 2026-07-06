package com.tlu.hrm.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.Timesheet;
import com.tlu.hrm.model.TimesheetDetail;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.repository.TimesheetRepository;
import com.tlu.hrm.service.ai.AiTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class GetStaffTimesheetTool implements AiTool {

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public String getName() {
        return "getStaffTimesheet";
    }

    @Override
    public String getDescription() {
        return "Tra cứu thông tin chấm công, số buổi đi làm thực tế, số phút đi muộn/về sớm và giờ tăng ca của nhân viên khác (dành cho quản lý/admin).";
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
            },
            "month": {
              "type": "integer",
              "description": "Tháng cần tra cứu (1 đến 12). Mặc định là tháng hiện tại."
            },
            "year": {
              "type": "integer",
              "description": "Năm cần tra cứu. Mặc định là năm hiện tại."
            }
          }
        }
        """;
    }

    @Override
    public String execute(JsonNode arguments, User currentUser) {
        boolean isManagerOrAdmin = currentUser.getUserRoles().stream()
                .anyMatch(ur -> "ROLE_ADMIN".equals(ur.getRole().getName())
                        || "HR_MANAGER".equals(ur.getRole().getName()));

        if (!isManagerOrAdmin) {
            return "{\"status\": \"error\", \"message\": \"Bạn không có quyền thực hiện hành động này.\"}";
        }

        String staffCode = arguments.has("staffCode") ? arguments.get("staffCode").asText() : null;
        String name = arguments.has("name") ? arguments.get("name").asText() : null;

        Staff targetStaff = null;
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

        if (targetStaff == null) {
            return "{\"status\": \"success\", \"message\": \"Không tìm thấy nhân viên phù hợp với tiêu chí tra cứu.\"}";
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
                targetStaff.getId(), start, end);

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
        sb.append("\"staffName\":\"").append(escapeJson(targetStaff.getDisplayName())).append("\",");
        sb.append("\"staffCode\":\"").append(escapeJson(targetStaff.getStaffCode())).append("\",");
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
