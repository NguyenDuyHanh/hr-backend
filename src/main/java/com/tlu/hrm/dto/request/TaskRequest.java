package com.tlu.hrm.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TaskRequest {
    private UUID id;
    private String name;
    private String code;
    private String description;

    private Integer priority;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double estimateHour;
    private UUID projectId;
    private UUID activityId;
    private UUID statusId;
    private UUID assigneeId;
}
