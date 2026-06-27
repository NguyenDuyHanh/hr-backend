package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import com.tlu.hrm.enums.CandidateStatus;

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

    @Column(name = "address")
    private String address;

    @Column(name = "cv_file_url")
    private String cvFileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CandidateStatus status; // SCREENING, INTERVIEW, QUALIFIED, WAITING, ONBOARDED, REJECTED
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id")
    private Recruitment recruitment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
