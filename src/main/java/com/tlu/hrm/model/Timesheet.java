package com.tlu.hrm.model;

import com.tlu.hrm.enums.TimesheetStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_timesheet", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"staff_id", "working_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Timesheet extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "working_date", nullable = false)
    private LocalDate workingDate;

    @Column(name = "total_work_ratio", nullable = false)
    private Double totalWorkRatio = 0.0;

    @Column(name = "standard_hours", nullable = false)
    private Double standardHours = 0.0;

    @Column(name = "overtime_hours", nullable = false)
    private Double overtimeHours = 0.0;

    @Column(name = "weekend_overtime_hours", nullable = false)
    private Double weekendOvertimeHours = 0.0;

    @Column(name = "holiday_overtime_hours", nullable = false)
    private Double holidayOvertimeHours = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TimesheetStatus status = TimesheetStatus.SUBMITTED;

    @Column(name = "note")
    private String note;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimesheetDetail> details = new ArrayList<>();
}
