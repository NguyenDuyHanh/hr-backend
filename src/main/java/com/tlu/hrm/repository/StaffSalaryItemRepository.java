package com.tlu.hrm.repository;

import com.tlu.hrm.model.StaffSalaryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StaffSalaryItemRepository extends JpaRepository<StaffSalaryItem, UUID>, JpaSpecificationExecutor<StaffSalaryItem> {
    List<StaffSalaryItem> findByStaffId(UUID staffId);
}
