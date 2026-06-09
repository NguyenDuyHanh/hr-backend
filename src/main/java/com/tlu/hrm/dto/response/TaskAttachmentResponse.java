package com.tlu.hrm.dto.response;

import com.tlu.hrm.model.TaskAttachment;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class TaskAttachmentResponse {
    private UUID id;
    private String name;
    private Long size;
    private String filePath;

    public TaskAttachmentResponse() {}

    public TaskAttachmentResponse(TaskAttachment entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.name = entity.getName();
            this.size = entity.getSize();
            this.filePath = entity.getFilePath();
        }
    }
}
