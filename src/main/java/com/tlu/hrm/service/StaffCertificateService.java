package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.StaffCertificateDto;
import com.tlu.hrm.enums.QualificationType;

import java.util.List;
import java.util.UUID;

public interface StaffCertificateService {
    List<StaffCertificateDto> getCertificatesByStaffId(UUID staffId, QualificationType type);
    StaffCertificateDto createCertificate(StaffCertificateDto dto);
    StaffCertificateDto updateCertificate(UUID id, StaffCertificateDto dto);
    void deleteCertificate(UUID id);
}
