package com.tlu.hrm.repository;

import com.tlu.hrm.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<Staff, UUID> {
    Staff findFirstByOrderByStaffCodeDesc();
    @org.springframework.data.jpa.repository.Query("SELECT s.staffCode FROM Staff s WHERE s.staffCode LIKE 'NV%_%'")
    java.util.List<String> findMaxValidStaffCode();
}
