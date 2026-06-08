package com.tlu.hrm.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ProjectActivityRequest {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer displayOrder;
}
