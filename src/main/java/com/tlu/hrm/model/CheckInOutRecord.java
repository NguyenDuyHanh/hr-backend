package com.tlu.hrm.model;

import com.tlu.hrm.enums.CheckInOutType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_check_in_out_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckInOutRecord extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "record_time", nullable = false)
    private LocalDateTime recordTime;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "photo_url")
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private CheckInOutType recordType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shift_id")
    private ShiftWork shift;
}
