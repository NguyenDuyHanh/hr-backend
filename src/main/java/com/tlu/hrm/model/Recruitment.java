package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.tlu.hrm.enums.RecruitmentStatus;

@Entity
@Table(name = "tbl_recruitment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recruitment extends BaseModel {

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    private RecruitmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_approve_cv")
    private Staff personApproveCV;
}
