package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.ShiftWork;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftWorkDto {
    private UUID id;
    private String code;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double workRatio;
    private String description;

    public ShiftWorkDto(ShiftWork entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.code = entity.getCode();
            this.name = entity.getName();
            this.startTime = entity.getStartTime();
            this.endTime = entity.getEndTime();
            this.workRatio = entity.getWorkRatio();
            this.description = entity.getDescription();
        }
    }
}
