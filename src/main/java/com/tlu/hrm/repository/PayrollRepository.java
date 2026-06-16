package com.tlu.hrm.repository;

import com.tlu.hrm.model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, UUID>, JpaSpecificationExecutor<Payroll> {
    List<Payroll> findByPayrollPeriodId(UUID periodId);
}
