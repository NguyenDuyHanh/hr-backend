package com.tlu.hrm.dto.response;

import com.tlu.hrm.model.ProjectActivity;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class ProjectActivityResponse {
    private UUID id;
    private UUID projectId;
    private String projectName;
    private String name;
    private String code;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer displayOrder;

    public ProjectActivityResponse() {}

    public ProjectActivityResponse(ProjectActivity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.projectId = entity.getProject() != null ? entity.getProject().getId() : null;
            this.projectName = entity.getProject() != null ? entity.getProject().getName() : null;
            this.name = entity.getName();
            this.code = entity.getCode();
            this.description = entity.getDescription();
            this.startTime = entity.getStartTime();
            this.endTime = entity.getEndTime();
            this.displayOrder = entity.getDisplayOrder();
        }
    }
}
