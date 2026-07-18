package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.NotificationRecipient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecipientDto {
    private UUID id;
    private UUID notificationId;
    private NotificationDto notification;
    private UUID userId;
    private String username;
    private LocalDateTime receivedAt;
    private LocalDateTime readAt;

    public NotificationRecipientDto(NotificationRecipient entity) {
        if (entity != null) {
            this.id = entity.getId();
            if (entity.getNotification() != null) {
                this.notificationId = entity.getNotification().getId();
                this.notification = new NotificationDto(entity.getNotification());
            }
            if (entity.getUser() != null) {
                this.userId = entity.getUser().getId();
                this.username = entity.getUser().getUsername();
            }
            this.receivedAt = entity.getReceivedAt();
            this.readAt = entity.getReadAt();
        }
    }
}
