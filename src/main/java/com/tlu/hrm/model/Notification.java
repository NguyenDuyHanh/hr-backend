package com.tlu.hrm.model;

import com.tlu.hrm.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tbl_notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseModel {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", length = 1000, nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 50)
    private NotificationType notificationType;

    @Column(name = "target_object_id")
    private UUID targetObjectId;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "is_global")
    private Boolean isGlobal = false;
}
