package com.tlu.hrm.repository;

import com.tlu.hrm.enums.QualificationType;
import com.tlu.hrm.model.StaffCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StaffCertificateRepository extends JpaRepository<StaffCertificate, UUID> {
    List<StaffCertificate> findByStaffIdAndIsDeletedFalse(UUID staffId);
    List<StaffCertificate> findByStaffIdAndTypeAndIsDeletedFalse(UUID staffId, QualificationType type);
}
