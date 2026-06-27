package com.tlu.hrm.dto.request;

import com.tlu.hrm.enums.LeaveApprovalStatus;
import com.tlu.hrm.enums.LeaveType;
import com.tlu.hrm.model.LeaveRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDto {
    private UUID id;
    private UUID requestStaffId;
    private String requestStaffName;
    private String requestStaffCode;
    private String departmentName;
    
    private UUID approvalStaffId;
    private String approvalStaffName;
    
    private LeaveType leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDate requestDate;
    private String requestReason;
    private LeaveApprovalStatus approvalStatus;
    
    private Double totalDays;
    
    private Boolean halfDayLeave;
    
    private UUID shiftWorkId;
    private String shiftWorkName;
    
    private String rejectReason;

    public LeaveRequestDto(LeaveRequest entity) {
        if (entity != null) {
            this.id = entity.getId();
            if (entity.getRequestStaff() != null) {
                this.requestStaffId = entity.getRequestStaff().getId();
                this.requestStaffName = entity.getRequestStaff().getDisplayName();
                this.requestStaffCode = entity.getRequestStaff().getStaffCode();
                if (entity.getRequestStaff().getDepartment() != null) {
                    this.departmentName = entity.getRequestStaff().getDepartment().getName();
                }
            }
            if (entity.getApprovalStaff() != null) {
                this.approvalStaffId = entity.getApprovalStaff().getId();
                this.approvalStaffName = entity.getApprovalStaff().getDisplayName();
            }
            this.leaveType = entity.getLeaveType();
            this.fromDate = entity.getFromDate();
            this.toDate = entity.getToDate();
            this.requestDate = entity.getRequestDate();
            this.requestReason = entity.getRequestReason();
            this.approvalStatus = entity.getApprovalStatus();
            this.totalDays = entity.getTotalDays();
            this.halfDayLeave = entity.getHalfDayLeave();
            if (entity.getShiftWork() != null) {
                this.shiftWorkId = entity.getShiftWork().getId();
                this.shiftWorkName = entity.getShiftWork().getName();
            }
            this.rejectReason = entity.getRejectReason();
        }
    }
}
