package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "status")
    private Integer status; // Trạng thái tin tuyển dụng (0: Nháp, 1: Đang tuyển, 2: Tạm dừng, 3: Đã đóng)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_approve_cv")
    private Staff personApproveCV;
}
