package com.tlu.hrm.model;

import com.tlu.hrm.enums.LeaveApprovalStatus;
import com.tlu.hrm.enums.LeaveType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tbl_leave_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_staff_id", nullable = false)
    private Staff requestStaff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_staff_id")
    private Staff approvalStaff;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "request_reason")
    private String requestReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private LeaveApprovalStatus approvalStatus = LeaveApprovalStatus.PENDING;

    @Column(name = "total_days", nullable = false)
    private Double totalDays = 0.0;

    @Column(name = "total_hours", nullable = false)
    private Double totalHours = 0.0;

    @Column(name = "half_day_leave")
    private Boolean halfDayLeave = false;

    @Column(name = "half_day_leave_start")
    private Boolean halfDayLeaveStart = false;

    @Column(name = "half_day_leave_end")
    private Boolean halfDayLeaveEnd = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_work_start_id")
    private ShiftWork shiftWorkStart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_work_end_id")
    private ShiftWork shiftWorkEnd;

    @Column(name = "reject_reason")
    private String rejectReason;
}
