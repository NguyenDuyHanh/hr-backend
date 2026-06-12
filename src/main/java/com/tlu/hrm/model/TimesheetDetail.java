package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_timesheet_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetDetail extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id", nullable = false)
    private Timesheet timesheet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shift_id", nullable = false)
    private ShiftWork shift;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "ip_check_in")
    private String ipCheckIn;

    @Column(name = "photo_check_in_url")
    private String photoCheckInUrl;

    @Column(name = "ip_check_out")
    private String ipCheckOut;

    @Column(name = "photo_check_out_url")
    private String photoCheckOutUrl;

    @Column(name = "late_minutes")
    private Integer lateMinutes = 0;

    @Column(name = "early_minutes")
    private Integer earlyMinutes = 0;

    @Column(name = "work_ratio")
    private Double workRatio = 0.0;
}
