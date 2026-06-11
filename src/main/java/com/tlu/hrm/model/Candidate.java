package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tbl_candidate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Candidate extends BaseModel {

    @Column(name = "candidate_code")
    private String candidateCode;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "current_residence")
    private String currentResidence;

    @Column(name = "cv_file_path")
    private String cvFilePath;

    @Column(name = "status")
    private Integer status; // 0: Sơ tuyển, 1: Phỏng vấn, 2: Đạt yêu cầu, 3: Chờ việc, 4: Đã onboard, 5: Từ
                            // chối
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id")
    private Recruitment recruitment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @Column(name = "onboard_status")
    private Integer onboardStatus = 0; // 0: Chưa onboard, 1: Đã onboard

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
