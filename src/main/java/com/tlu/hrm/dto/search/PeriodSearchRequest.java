package com.tlu.hrm.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PeriodSearchRequest extends SearchDto {
    private String code;
    private String name;
    private Integer month;
    private Integer year;
    private LocalDate fromDate;
    private LocalDate toDate;
}
