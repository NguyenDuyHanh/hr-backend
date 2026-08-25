package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.StaffCertificateDto;
import com.tlu.hrm.enums.QualificationType;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.StaffCertificate;
import com.tlu.hrm.repository.StaffCertificateRepository;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.service.StaffCertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class StaffCertificateServiceImpl implements StaffCertificateService {

    @Autowired
    private StaffCertificateRepository staffCertificateRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StaffCertificateDto> getCertificatesByStaffId(UUID staffId, QualificationType type) {
        List<StaffCertificate> list;
        if (type != null) {
            list = staffCertificateRepository.findByStaffIdAndTypeAndIsDeletedFalse(staffId, type);
        } else {
            list = staffCertificateRepository.findByStaffIdAndIsDeletedFalse(staffId);
        }
        return list.stream()
                .map(StaffCertificateDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public StaffCertificateDto createCertificate(StaffCertificateDto dto) {
        Staff staff = staffRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại"));

        StaffCertificate cert = new StaffCertificate();
        cert.setStaff(staff);
        mapDtoToEntity(dto, cert);

        StaffCertificate saved = staffCertificateRepository.save(cert);
        return new StaffCertificateDto(saved);
    }

    @Override
    public StaffCertificateDto updateCertificate(UUID id, StaffCertificateDto dto) {
        StaffCertificate cert = staffCertificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bằng cấp chứng chỉ không tồn tại"));

        mapDtoToEntity(dto, cert);

        StaffCertificate saved = staffCertificateRepository.save(cert);
        return new StaffCertificateDto(saved);
    }

    @Override
    public void deleteCertificate(UUID id) {
        StaffCertificate cert = staffCertificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bằng cấp chứng chỉ không tồn tại"));

        cert.setIsDeleted(true);
        staffCertificateRepository.save(cert);
    }

    private void mapDtoToEntity(StaffCertificateDto dto, StaffCertificate cert) {
        cert.setType(dto.getType());
        cert.setCertificateName(dto.getCertificateName());
        cert.setInstitution(dto.getInstitution());
        cert.setMajor(dto.getMajor());
        cert.setDegreeLevel(dto.getDegreeLevel());
        cert.setIssueDate(dto.getIssueDate());
        cert.setExpiryDate(dto.getExpiryDate());
        cert.setGrade(dto.getGrade());
        cert.setCredentialId(dto.getCredentialId());
        cert.setFileUrl(dto.getFileUrl());
        cert.setNote(dto.getNote());
    }
}
