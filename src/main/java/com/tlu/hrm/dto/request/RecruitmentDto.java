package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.Recruitment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentDto {
    private UUID id;
    private LocalDateTime createDate;
    private String code;
    private String name;
    private String description;
    private Integer status;
    private UUID personApproveCVId;
    private String personApproveCVName;

    public RecruitmentDto(Recruitment entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.createDate = entity.getCreateDate();
            this.code = entity.getCode();
            this.name = entity.getName();
            this.description = entity.getDescription();
            this.status = entity.getStatus();
            if (entity.getPersonApproveCV() != null) {
                this.personApproveCVId = entity.getPersonApproveCV().getId();
                this.personApproveCVName = entity.getPersonApproveCV().getDisplayName();
            }
        }
    }
}
