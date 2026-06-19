package com.tlu.hrm.dto.request;

import com.tlu.hrm.enums.CandidateStatus;
import com.tlu.hrm.model.Candidate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDto {
    private UUID id;
    private LocalDateTime createDate;
    private String candidateCode;
    private String displayName;
    private String gender;
    private LocalDate birthDate;
    private String email;
    private String phoneNumber;

    private String currentResidence;
    private String cvFilePath;
    private CandidateStatus status;
    private String note;

    private UUID recruitmentId;
    private String recruitmentName;
    private String recruitmentCode;

    private UUID departmentId;
    private String departmentName;

    private UUID positionId;
    private String positionName;



    private Integer onboardStatus;

    public CandidateDto(Candidate entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.createDate = entity.getCreateDate();
            this.candidateCode = entity.getCandidateCode();
            this.displayName = entity.getDisplayName();
            this.gender = entity.getGender();
            this.birthDate = entity.getBirthDate();
            this.email = entity.getEmail();
            this.phoneNumber = entity.getPhoneNumber();

            this.currentResidence = entity.getCurrentResidence();
            this.cvFilePath = entity.getCvFilePath();
            this.status = entity.getStatus();
            this.onboardStatus = entity.getOnboardStatus();
            this.note = entity.getNote();

            if (entity.getRecruitment() != null) {
                this.recruitmentId = entity.getRecruitment().getId();
                this.recruitmentName = entity.getRecruitment().getName();
                this.recruitmentCode = entity.getRecruitment().getCode();
            }
            if (entity.getDepartment() != null) {
                this.departmentId = entity.getDepartment().getId();
                this.departmentName = entity.getDepartment().getName();
            }
            if (entity.getPosition() != null) {
                this.positionId = entity.getPosition().getId();
                this.positionName = entity.getPosition().getName();
            }

        }
    }
}
