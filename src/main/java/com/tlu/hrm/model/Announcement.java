package com.tlu.hrm.model;

import com.tlu.hrm.enums.AnnouncementCategory;
import com.tlu.hrm.enums.AnnouncementStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_announcement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Announcement extends BaseModel {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "code", length = 50, unique = true)
    private String code;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "title_image_url", length = 1000)
    private String titleImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50, nullable = false)
    private AnnouncementCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private AnnouncementStatus status = AnnouncementStatus.DRAFT;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(name = "attachments", length = 1000)
    private String attachments;

    @Column(name = "target_dept_id")
    private UUID targetDeptId;
}
