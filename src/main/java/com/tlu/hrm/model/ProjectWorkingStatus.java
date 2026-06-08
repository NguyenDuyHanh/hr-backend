package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tbl_project_working_status")
@Getter
@Setter
public class ProjectWorkingStatus extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;  // Sort order trong Kanban

    @Column(name = "color")
    private String color;         // Hex color
}
