package com.tlu.hrm.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class TaskRequest {
    private UUID id;
    private String name;
    private Long code;
    private String description;
    private String comment;
    private Integer priority;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double estimateHour;
    private UUID projectId;
    private UUID activityId;
    private UUID statusId;
    private UUID assigneeId;
    private Set<UUID> followerIds;
}
