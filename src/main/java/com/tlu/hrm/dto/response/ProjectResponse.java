package com.tlu.hrm.dto.response;

import com.tlu.hrm.model.Project;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class ProjectResponse {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private Boolean isFinished;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ProjectStaffDto> staffs = new ArrayList<>();
    private String createdBy;

    public ProjectResponse() {
    }

    public ProjectResponse(Project entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.name = entity.getName();
            this.code = entity.getCode();
            this.description = entity.getDescription();

            this.isFinished = entity.getIsFinished();
            this.startDate = entity.getStartDate();
            this.endDate = entity.getEndDate();
            this.createdBy = entity.getCreatedBy();
            if (entity.getProjectStaffs() != null) {
                this.staffs = entity.getProjectStaffs().stream()
                        .filter(ps -> ps.getVoided() == null || !ps.getVoided())
                        .map(ProjectStaffDto::new)
                        .collect(Collectors.toList());
            }
        }
    }
}
