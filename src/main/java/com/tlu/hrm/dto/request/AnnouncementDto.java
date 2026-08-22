package com.tlu.hrm.dto.request;

import com.tlu.hrm.enums.AnnouncementCategory;
import com.tlu.hrm.enums.AnnouncementStatus;
import com.tlu.hrm.model.Announcement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDto {
    private UUID id;
    private String title;
    private String content;
    private String titleImageUrl;
    private AnnouncementCategory category;
    private AnnouncementStatus status;
    private LocalDateTime publishDate;
    private String attachments;
    private UUID targetDeptId;
    private LocalDateTime createDate;
    private String createdBy;
    private Boolean isRead;
    private String code;

    public AnnouncementDto(Announcement entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.title = entity.getTitle();
            this.content = entity.getContent();
            this.titleImageUrl = entity.getTitleImageUrl();
            this.category = entity.getCategory();
            this.status = entity.getStatus();
            this.publishDate = entity.getPublishDate();
            this.attachments = entity.getAttachments();
            this.targetDeptId = entity.getTargetDeptId();
            this.createDate = entity.getCreateDate();
            this.createdBy = entity.getCreatedBy();
            this.code = entity.getCode();
        }
    }
}
