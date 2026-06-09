package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tbl_task_history")
@Getter
@Setter
public class TaskHistory extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifier_id")
    private Staff modifier;

    @Column(name = "event", columnDefinition = "TEXT")
    private String event; // log thay đổi (JSON)

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment; // nội dung bình luận
}
