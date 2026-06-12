package com.tlu.hrm.dto.search;

import com.tlu.hrm.enums.TimesheetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TimesheetSearchRequest extends SearchDto {
    private UUID staffId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private TimesheetStatus status;
}
