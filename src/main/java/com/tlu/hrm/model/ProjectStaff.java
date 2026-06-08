package com.tlu.hrm.model;

import com.tlu.hrm.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "tbl_project_staff")
@Getter
@Setter
public class ProjectStaff extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_role")
    private ProjectRole projectRole;


    @Column(name = "joined_date")
    private LocalDate joinedDate;
}
