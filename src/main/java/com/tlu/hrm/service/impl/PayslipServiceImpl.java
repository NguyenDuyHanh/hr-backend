package com.tlu.hrm.service.impl;

import com.tlu.hrm.enums.PaidStatus;
import com.tlu.hrm.enums.SalaryCalculationType;
import com.tlu.hrm.enums.SalaryItemType;
import com.tlu.hrm.enums.TimesheetStatus;
import com.tlu.hrm.model.*;
import com.tlu.hrm.repository.TimesheetRepository;
import com.tlu.hrm.repository.SalaryItemRepository;
import com.tlu.hrm.service.PayslipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PayslipServiceImpl implements PayslipService {

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private SalaryItemRepository salaryItemRepository;

    @Override
    public Payslip calculateStaffPayslip(
            Staff staff, 
            Payroll payroll, 
            LocalDate start, 
            LocalDate end, 
            double standardWorkDays, 
            List<StaffSalaryItem> staffSalaryItems
    ) {
        // 1. Lấy danh sách bảng công đã duyệt trong khoảng thời gian
        List<Timesheet> timesheets = timesheetRepository
                .findActiveTimesheetsByStaffIdAndWorkingDateBetweenAndStatus(staff.getId(), start, end, TimesheetStatus.APPROVED);

        double totalWorkDays = 0.0;
        double totalOtHours = 0.0;

        for (Timesheet ts : timesheets) {
            totalWorkDays += ts.getTotalWorkRatio() != null ? ts.getTotalWorkRatio() : 0.0;
            totalOtHours += ts.getOvertimeHours() != null ? ts.getOvertimeHours() : 0.0;
        }

        // 2. Tạo đối tượng Payslip
        Payslip payslip = new Payslip();
        payslip.setPayroll(payroll);
        payslip.setStaff(staff);
        payslip.setTotalWorkDays(totalWorkDays);
        payslip.setTotalOtHours(totalOtHours);
        payslip.setPaidStatus(PaidStatus.UNPAID);

        double totalIncome = 0.0;
        double totalDeduction = 0.0;
        List<PayslipItem> items = new ArrayList<>();

        // 3. Tính toán từng khoản lương
        for (StaffSalaryItem ssi : staffSalaryItems) {
            SalaryItem salaryItem = ssi.getSalaryItem();
            if (salaryItem == null) {
                continue;
            }

            double amount = ssi.getAmount() != null ? ssi.getAmount() : 0.0;
            double calculatedValue = 0.0;

            if (salaryItem.getCode() != null && 
                ("OT".equalsIgnoreCase(salaryItem.getCode()) || 
                 "LUONG_OT".equalsIgnoreCase(salaryItem.getCode()) ||
                 "OVERTIME".equalsIgnoreCase(salaryItem.getCode()) ||
                 salaryItem.getCode().toUpperCase().contains("TANG_CA"))) {
                double baseSalary = amount;
                if (baseSalary <= 0) {
                    for (StaffSalaryItem item : staffSalaryItems) {
                        if (item.getSalaryItem() != null && 
                            ("LUONG_CO_BAN".equalsIgnoreCase(item.getSalaryItem().getCode()) || 
                             "LCB".equalsIgnoreCase(item.getSalaryItem().getCode()) ||
                             item.getSalaryItem().getName().toLowerCase().contains("lương cơ bản"))) {
                            baseSalary = item.getAmount() != null ? item.getAmount() : 0.0;
                            break;
                        }
                    }
                }
                amount = baseSalary; // Lưu lương cơ bản gốc làm căn cứ tính OT
                if (standardWorkDays > 0) {
                    calculatedValue = (baseSalary / standardWorkDays / 8.0) * 1.5 * totalOtHours;
                } else {
                    calculatedValue = 0.0;
                }
            } else {
                SalaryCalculationType calcType = salaryItem.getCalculationType();
                if (calcType == null) {
                    calcType = SalaryCalculationType.FIXED;
                }

                switch (calcType) {
                    case FIXED:
                        calculatedValue = amount;
                        break;
                    case BY_STANDARD_DAYS:
                        if (standardWorkDays > 0) {
                            calculatedValue = (amount / standardWorkDays) * totalWorkDays;
                        } else {
                            calculatedValue = 0.0;
                        }
                        break;
                    case DAILY_MULTIPLIED:
                        calculatedValue = amount * totalWorkDays;
                        break;
                    default:
                        calculatedValue = amount;
                        break;
                }
            }

            // Làm tròn kết quả tính toán đến 2 chữ số thập phân
            calculatedValue = Math.round(calculatedValue * 100.0) / 100.0;

            PayslipItem payslipItem = new PayslipItem();
            payslipItem.setPayslip(payslip);
            payslipItem.setSalaryItem(salaryItem);
            payslipItem.setName(salaryItem.getName());
            payslipItem.setCalculatedValue(calculatedValue);
            payslipItem.setAmount(amount);

            items.add(payslipItem);

            SalaryItemType itemType = salaryItem.getType();
            if (itemType == SalaryItemType.INCOME) {
                totalIncome += calculatedValue;
            } else if (itemType == SalaryItemType.DEDUCTION) {
                totalDeduction += calculatedValue;
            }
        }

        payslip.setItems(items);
        payslip.setTotalIncome(Math.round(totalIncome * 100.0) / 100.0);
        payslip.setTotalDeduction(Math.round(totalDeduction * 100.0) / 100.0);
        
        double netSalary = totalIncome - totalDeduction;
        payslip.setNetSalary(Math.round(netSalary * 100.0) / 100.0);

        return payslip;
    }
}
