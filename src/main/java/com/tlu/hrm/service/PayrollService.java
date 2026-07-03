package com.tlu.hrm.service;

import com.tlu.hrm.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PayrollService {

    Payroll createPayroll(UUID periodId, String name, String code, String description);

    List<Payroll> getPayrollsByPeriod(UUID periodId);

    List<Payroll> getAllPayrolls();

    List<Payslip> calculatePayroll(UUID payrollId);

    List<Payslip> getPayrollDetails(UUID payrollId);

    Payroll confirmPayroll(UUID payrollId);

    Payroll unconfirmPayroll(UUID payrollId);

    void deletePayroll(UUID payrollId);

    Payslip getMyPayslip(UUID periodId, User currentUser);

    Payslip updatePayslip(UUID id, com.tlu.hrm.enums.PaidStatus paidStatus, String note);
}
