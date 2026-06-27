package com.tlu.hrm.dto.response;

import com.tlu.hrm.model.Task;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class TaskResponse {
    private UUID id;
    private String name;
    private String code;
    private String description;

    private Integer priority;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double estimateHour;
    
    private UUID projectId;
    private String projectName;
    private String projectCode;
    
    private UUID activityId;
    private String activityName;
    
    private UUID statusId;
    private String statusName;
    private String statusCode;
    private String statusColor;
    
    private UUID assigneeId;
    private String assigneeName;
    

    private List<TaskAttachmentResponse> attachments = new ArrayList<>();
    
    private LocalDateTime createDate;
    private String createdBy;
    private LocalDateTime modifyDate;
    private String modifiedBy;



    public TaskResponse() {}

    public TaskResponse(Task entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.name = entity.getName();
            
            this.code = entity.getCode();
            
            this.description = entity.getDescription();

            this.priority = entity.getPriority();
            this.startTime = entity.getStartTime();
            this.endTime = entity.getEndTime();
            this.estimateHour = entity.getEstimateHour();
            this.createDate = entity.getCreateDate();
            this.createdBy = entity.getCreatedBy();
            this.modifyDate = entity.getModifyDate();
            this.modifiedBy = entity.getModifiedBy();

            if (entity.getProject() != null) {
                this.projectId = entity.getProject().getId();
                this.projectName = entity.getProject().getName();
                this.projectCode = entity.getProject().getCode();
            }

            if (entity.getActivity() != null) {
                this.activityId = entity.getActivity().getId();
                this.activityName = entity.getActivity().getName();
            }

            if (entity.getStatus() != null) {
                this.statusId = entity.getStatus().getId();
                this.statusName = entity.getStatus().getName();
                this.statusCode = entity.getStatus().getCode();
                this.statusColor = entity.getStatus().getColor();
            }

            if (entity.getAssignee() != null) {
                this.assigneeId = entity.getAssignee().getId();
                this.assigneeName = entity.getAssignee().getDisplayName();
            }



            if (entity.getAttachments() != null) {
                this.attachments = entity.getAttachments().stream()
                        .filter(a -> a.getIsDeleted() == null || !a.getIsDeleted())
                        .map(TaskAttachmentResponse::new)
                        .collect(Collectors.toList());
            }
        }
    }
}
