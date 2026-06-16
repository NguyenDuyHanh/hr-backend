package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.Period;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodDto {
    private UUID id;
    private String code;
    private String name;
    private String period;
    private Integer month;
    private Integer year;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Double standardWorkDays;
    private String description;

    public PeriodDto(Period entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.code = entity.getCode();
            this.name = entity.getName();
            this.period = entity.getPeriod();
            this.month = entity.getMonth();
            this.year = entity.getYear();
            this.fromDate = entity.getFromDate();
            this.toDate = entity.getToDate();
            this.standardWorkDays = entity.getStandardWorkDays();
            this.description = entity.getDescription();
        }
    }
}
