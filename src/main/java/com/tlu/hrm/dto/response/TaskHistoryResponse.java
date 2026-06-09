package com.tlu.hrm.dto.response;

import com.tlu.hrm.model.TaskHistory;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TaskHistoryResponse {
    private UUID id;
    private UUID taskId;
    private UUID modifierId;
    private String modifierName;
    private String event;
    private String comment;
    private LocalDateTime createDate;

    public TaskHistoryResponse() {}

    public TaskHistoryResponse(TaskHistory entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.event = entity.getEvent();
            this.comment = entity.getComment();
            this.createDate = entity.getCreateDate();
            if (entity.getTask() != null) {
                this.taskId = entity.getTask().getId();
            }
            if (entity.getModifier() != null) {
                this.modifierId = entity.getModifier().getId();
                this.modifierName = entity.getModifier().getDisplayName();
            }
        }
    }
}
