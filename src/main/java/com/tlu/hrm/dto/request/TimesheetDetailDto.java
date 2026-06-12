package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.TimesheetDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetDetailDto {
    private UUID id;
    private UUID timesheetId;
    private UUID shiftId;
    private String shiftName;
    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String ipCheckIn;
    private String photoCheckInUrl;
    private String ipCheckOut;
    private String photoCheckOutUrl;
    private Integer lateMinutes;
    private Integer earlyMinutes;
    private Double workRatio;
    private ShiftDto shift;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShiftDto {
        private UUID id;
        private String code;
        private String name;
        private String description;
        private LocalTime startTime;
        private LocalTime endTime;

        public ShiftDto(com.tlu.hrm.model.ShiftWork entity) {
            if (entity != null) {
                this.id = entity.getId();
                this.code = entity.getCode();
                this.name = entity.getName();
                this.description = entity.getDescription();
                this.startTime = entity.getStartTime();
                this.endTime = entity.getEndTime();
            }
        }
    }

    public TimesheetDetailDto(TimesheetDetail entity) {
        if (entity != null) {
            this.id = entity.getId();
            if (entity.getTimesheet() != null) {
                this.timesheetId = entity.getTimesheet().getId();
            }
            if (entity.getShift() != null) {
                this.shiftId = entity.getShift().getId();
                this.shiftName = entity.getShift().getName();
                this.shiftStartTime = entity.getShift().getStartTime();
                this.shiftEndTime = entity.getShift().getEndTime();
                this.shift = new ShiftDto(entity.getShift());
            }
            this.checkInTime = entity.getCheckInTime();
            this.checkOutTime = entity.getCheckOutTime();
            this.ipCheckIn = entity.getIpCheckIn();
            this.photoCheckInUrl = entity.getPhotoCheckInUrl();
            this.ipCheckOut = entity.getIpCheckOut();
            this.photoCheckOutUrl = entity.getPhotoCheckOutUrl();
            this.lateMinutes = entity.getLateMinutes();
            this.earlyMinutes = entity.getEarlyMinutes();
            this.workRatio = entity.getWorkRatio();
        }
    }
}
