package com.tlu.hrm.service;

import com.tlu.hrm.model.Payslip;
import com.tlu.hrm.model.Payroll;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.StaffSalaryItem;

import java.time.LocalDate;
import java.util.List;

public interface PayslipService {
    Payslip calculateStaffPayslip(
            Staff staff, 
            Payroll payroll, 
            LocalDate start, 
            LocalDate end, 
            double standardWorkDays, 
            List<StaffSalaryItem> staffSalaryItems
    );
}
