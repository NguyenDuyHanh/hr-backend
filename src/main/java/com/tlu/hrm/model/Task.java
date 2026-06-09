package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "tbl_task")
@Getter
@Setter
public class Task extends BaseModel {

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "code")
    private Long code; // auto-increment per project

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "priority")
    private Integer priority; // 1=Low, 2=Medium, 3=High, 4=Urgent

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "estimate_hour")
    private Double estimateHour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private ProjectActivity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private ProjectWorkingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private Staff assignee; // người phụ trách

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tbl_task_follower", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "staff_id"))
    private Set<Staff> staffs = new HashSet<>(); // người theo dõi

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskAttachment> attachments = new HashSet<>();
}
