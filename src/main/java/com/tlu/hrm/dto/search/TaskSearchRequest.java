package com.tlu.hrm.dto.search;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TaskSearchRequest {
    private int pageIndex;
    private int pageSize;
    private String keyword;
    private UUID projectId;
    private UUID assigneeId;
    private UUID followerId;
    private List<UUID> statusIds;
    private List<UUID> activityIds;
    private List<Integer> priorities;
    private LocalDateTime startCreatedDate;
    private LocalDateTime endCreatedDate;
    private String sortBy;
    private Boolean sortDesc;
}
