package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tbl_task_attachment")
@Getter
@Setter
public class TaskAttachment extends BaseModel {

    @Column(name = "name")
    private String name;

    @Column(name = "size")
    private Long size;

    @Column(name = "file_path")
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;
}
