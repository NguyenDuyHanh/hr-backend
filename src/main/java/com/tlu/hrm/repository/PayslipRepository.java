package com.tlu.hrm.repository;

import com.tlu.hrm.model.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.tlu.hrm.enums.PayrollStatus;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, UUID>, JpaSpecificationExecutor<Payslip> {
    void deleteByPayrollId(UUID payrollId);
    Optional<Payslip> findByStaffIdAndPayrollId(UUID staffId, UUID payrollId);
    Optional<Payslip> findByStaffIdAndPayrollPayrollPeriodIdAndPayrollStatus(UUID staffId, UUID payrollPeriodId, PayrollStatus status);
}
