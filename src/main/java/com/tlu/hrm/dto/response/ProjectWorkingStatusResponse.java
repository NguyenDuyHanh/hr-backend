package com.tlu.hrm.dto.response;

import com.tlu.hrm.model.ProjectWorkingStatus;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class ProjectWorkingStatusResponse {
    private UUID id;
    private UUID projectId;
    private String projectName;
    private String name;
    private String code;
    private String description;
    private Integer displayOrder;
    private String color;

    public ProjectWorkingStatusResponse() {}

    public ProjectWorkingStatusResponse(ProjectWorkingStatus entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.projectId = entity.getProject() != null ? entity.getProject().getId() : null;
            this.projectName = entity.getProject() != null ? entity.getProject().getName() : null;
            this.name = entity.getName();
            this.code = entity.getCode();
            this.description = entity.getDescription();
            this.displayOrder = entity.getDisplayOrder();
            this.color = entity.getColor();
        }
    }
}
