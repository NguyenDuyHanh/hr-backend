package com.tlu.hrm.dto.search;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ProjectSearchRequest {
    private int pageIndex;
    private int pageSize;
    private String keyword;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isFinished;
    private UUID staffId;
}
