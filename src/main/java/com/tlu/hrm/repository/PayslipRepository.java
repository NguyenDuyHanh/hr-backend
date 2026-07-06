package com.tlu.hrm.repository;

import com.tlu.hrm.model.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tlu.hrm.enums.PayrollStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, UUID>, JpaSpecificationExecutor<Payslip> {
    
    @Modifying
    @Transactional
    @Query("UPDATE Payslip p SET p.isDeleted = true WHERE p.payroll.id = :payrollId")
    void deleteByPayrollId(@Param("payrollId") UUID payrollId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE tbl_payslip_item SET is_deleted = true WHERE payslip_id IN (SELECT id FROM tbl_payslip WHERE payroll_id = :payrollId)", nativeQuery = true)
    void deleteItemsByPayrollId(@Param("payrollId") UUID payrollId);

    @Query("SELECT p FROM Payslip p WHERE p.payroll.id = :payrollId AND (p.isDeleted = false OR p.isDeleted IS NULL)")
    List<Payslip> findByPayrollId(@Param("payrollId") UUID payrollId);

    @Query("SELECT p FROM Payslip p WHERE p.staff.id = :staffId AND p.payroll.id = :payrollId AND (p.isDeleted = false OR p.isDeleted IS NULL)")
    Optional<Payslip> findByStaffIdAndPayrollId(@Param("staffId") UUID staffId, @Param("payrollId") UUID payrollId);

    @Query("SELECT p FROM Payslip p WHERE p.staff.id = :staffId AND p.payroll.period.id = :periodId AND p.payroll.status = :status AND (p.isDeleted = false OR p.isDeleted IS NULL) AND (p.payroll.isDeleted = false OR p.payroll.isDeleted IS NULL)")
    Optional<Payslip> findByStaffIdAndPayrollPeriodIdAndPayrollStatus(@Param("staffId") UUID staffId, @Param("periodId") UUID periodId, @Param("status") PayrollStatus status);

    @Query("SELECT p FROM Payslip p WHERE p.staff.id = :staffId AND p.payroll.period.id = :periodId AND (p.isDeleted = false OR p.isDeleted IS NULL) AND (p.payroll.isDeleted = false OR p.payroll.isDeleted IS NULL)")
    Optional<Payslip> findByStaffIdAndPayrollPeriodId(@Param("staffId") UUID staffId, @Param("periodId") UUID periodId);
}

