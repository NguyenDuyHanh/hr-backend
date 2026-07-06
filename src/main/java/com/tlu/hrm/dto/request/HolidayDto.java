package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.Holiday;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayDto {
    private UUID id;
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private Integer year;
    private String description;

    public HolidayDto(Holiday entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.code = entity.getCode();
            this.name = entity.getName();
            this.startDate = entity.getStartDate();
            this.endDate = entity.getEndDate();
            this.totalDays = entity.getTotalDays();
            this.year = entity.getYear();
            this.description = entity.getDescription();
        }
    }
}
