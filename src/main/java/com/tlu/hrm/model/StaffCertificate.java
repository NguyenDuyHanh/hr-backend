package com.tlu.hrm.model;

import com.tlu.hrm.enums.QualificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tbl_staff_certificate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffCertificate extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private QualificationType type;

    @Column(name = "certificate_name", nullable = false)
    private String certificateName;

    @Column(name = "institution", nullable = false)
    private String institution;

    @Column(name = "major")
    private String major;

    @Column(name = "degree_level")
    private String degreeLevel;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "grade")
    private String grade;

    @Column(name = "credential_id")
    private String credentialId;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
