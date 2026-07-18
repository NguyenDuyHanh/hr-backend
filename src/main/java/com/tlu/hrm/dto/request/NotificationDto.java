package com.tlu.hrm.dto.request;

import com.tlu.hrm.enums.NotificationType;
import com.tlu.hrm.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private UUID id;
    private String title;
    private String content;
    private NotificationType notificationType;
    private UUID targetObjectId;
    private String linkUrl;
    private Boolean isGlobal;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;
    private Boolean isRead; // Trạng thái đã đọc đối với user hiện tại khi truy vấn danh sách

    public NotificationDto(Notification entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.title = entity.getTitle();
            this.content = entity.getContent();
            this.notificationType = entity.getNotificationType();
            this.targetObjectId = entity.getTargetObjectId();
            this.linkUrl = entity.getLinkUrl();
            this.isGlobal = entity.getIsGlobal();
            this.createDate = entity.getCreateDate();
            this.modifyDate = entity.getModifyDate();
        }
    }
}
