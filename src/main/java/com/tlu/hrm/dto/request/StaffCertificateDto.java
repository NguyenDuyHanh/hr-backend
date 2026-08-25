package com.tlu.hrm.dto.request;

import com.tlu.hrm.enums.QualificationType;
import com.tlu.hrm.model.StaffCertificate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffCertificateDto {
    private UUID id;
    private UUID staffId;
    private QualificationType type;
    private String certificateName;
    private String institution;
    private String major;
    private String degreeLevel;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String grade;
    private String credentialId;
    private String fileUrl;
    private String note;

    public StaffCertificateDto(StaffCertificate entity) {
        if (entity != null) {
            this.id = entity.getId();
            if (entity.getStaff() != null) {
                this.staffId = entity.getStaff().getId();
            }
            this.type = entity.getType();
            this.certificateName = entity.getCertificateName();
            this.institution = entity.getInstitution();
            this.major = entity.getMajor();
            this.degreeLevel = entity.getDegreeLevel();
            this.issueDate = entity.getIssueDate();
            this.expiryDate = entity.getExpiryDate();
            this.grade = entity.getGrade();
            this.credentialId = entity.getCredentialId();
            this.fileUrl = entity.getFileUrl();
            this.note = entity.getNote();
        }
    }
}
