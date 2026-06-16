package com.tlu.hrm.dto.request;

import com.tlu.hrm.enums.TimesheetStatus;
import com.tlu.hrm.model.Timesheet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetDto {
    private UUID id;
    private UUID staffId;
    private String staffName;
    private String staffCode;
    private String departmentName;
    private String positionName;
    private UUID periodId;
    private String periodName;
    private LocalDate workingDate;
    private Double totalWorkRatio;
    private Double standardHours;
    private Double overtimeHours;
    private TimesheetStatus status;
    private String note;
    private List<TimesheetDetailDto> details = new ArrayList<>();

    public TimesheetDto(Timesheet entity) {
        if (entity != null) {
            this.id = entity.getId();
            if (entity.getStaff() != null) {
                this.staffId = entity.getStaff().getId();
                this.staffName = entity.getStaff().getDisplayName();
                this.staffCode = entity.getStaff().getStaffCode();
                if (entity.getStaff().getDepartment() != null) {
                    this.departmentName = entity.getStaff().getDepartment().getName();
                }
                if (entity.getStaff().getPosition() != null) {
                    this.positionName = entity.getStaff().getPosition().getName();
                }
            }
            if (entity.getPeriod() != null) {
                this.periodId = entity.getPeriod().getId();
                this.periodName = entity.getPeriod().getName();
            }
            this.workingDate = entity.getWorkingDate();
            this.totalWorkRatio = entity.getTotalWorkRatio();
            this.standardHours = entity.getStandardHours();
            this.overtimeHours = entity.getOvertimeHours();
            this.status = entity.getStatus();
            this.note = entity.getNote();
            if (entity.getDetails() != null) {
                this.details = entity.getDetails().stream()
                        .map(TimesheetDetailDto::new)
                        .collect(Collectors.toList());
            }
        }
    }
}
