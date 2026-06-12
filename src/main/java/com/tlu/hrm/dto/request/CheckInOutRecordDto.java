package com.tlu.hrm.dto.request;

import com.tlu.hrm.enums.CheckInOutType;
import com.tlu.hrm.model.CheckInOutRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInOutRecordDto {
    private UUID id;
    private UUID staffId;
    private String staffName;
    private LocalDateTime recordTime;
    private String ipAddress;
    private Double latitude;
    private Double longitude;
    private String deviceType;
    private String photoUrl;
    private CheckInOutType recordType;
    private UUID shiftId;
    private String shiftName;

    public CheckInOutRecordDto(CheckInOutRecord entity) {
        if (entity != null) {
            this.id = entity.getId();
            if (entity.getStaff() != null) {
                this.staffId = entity.getStaff().getId();
                this.staffName = entity.getStaff().getDisplayName();
            }
            this.recordTime = entity.getRecordTime();
            this.ipAddress = entity.getIpAddress();
            this.latitude = entity.getLatitude();
            this.longitude = entity.getLongitude();
            this.deviceType = entity.getDeviceType();
            this.photoUrl = entity.getPhotoUrl();
            this.recordType = entity.getRecordType();
            if (entity.getShift() != null) {
                this.shiftId = entity.getShift().getId();
                this.shiftName = entity.getShift().getName();
            }
        }
    }
}
