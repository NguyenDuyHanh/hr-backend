package com.tlu.hrm.service.impl;

import com.tlu.hrm.enums.PayrollStatus;
import com.tlu.hrm.model.*;
import com.tlu.hrm.repository.*;
import com.tlu.hrm.service.PayrollService;
import com.tlu.hrm.service.PayslipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;

@Service
@Transactional
public class PayrollServiceImpl implements PayrollService {

    @Autowired
    private PeriodRepository periodRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StaffSalaryItemRepository staffSalaryItemRepository;

    @Autowired
    private PayslipService payslipService;

    @Override
    public Payroll createPayroll(UUID periodId, String name, String code, String description) {
        Period period = periodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Kỳ lương không tồn tại"));

        Payroll payroll = new Payroll();
        payroll.setName(name);
        payroll.setPayrollPeriod(period);
        if (code == null || code.trim().isEmpty()) {
            payroll.setCode(generatePayrollCode(name));
        } else {
            payroll.setCode(code.toUpperCase().trim());
        }
        payroll.setDescription(description);
        payroll.setStatus(PayrollStatus.DRAFT);
        payroll = payrollRepository.save(payroll);

        // Tự động tính toán lương và tạo phiếu lương cho tất cả nhân viên ngay khi tạo
        // Bảng lương
        calculatePayroll(payroll.getId());

        return payroll;
    }

    private String generatePayrollCode(String name) {
        if (name == null)
            return "PAYROLL_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String clean = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", "_")
                .toUpperCase();
        if (clean.length() > 50) {
            clean = clean.substring(0, 50);
        }
        return clean + "_" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    @Override
    public List<Payroll> getPayrollsByPeriod(UUID periodId) {
        return payrollRepository.findByPayrollPeriodId(periodId).stream()
                .filter(p -> p.getVoided() == null || !p.getVoided())
                .collect(Collectors.toList());
    }

    @Override
    public List<Payroll> getAllPayrolls() {
        return payrollRepository.findAll().stream()
                .filter(p -> p.getVoided() == null || !p.getVoided())
                .collect(Collectors.toList());
    }

    @Override
    public List<Payslip> calculatePayroll(UUID payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new IllegalArgumentException("Bảng lương không tồn tại"));

        if (payroll.getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException("Bảng lương đã được khóa, không thể tính toán lại.");
        }

        // 1. Xóa kết quả tính toán cũ của bảng lương này
        payslipRepository.deleteByPayrollId(payrollId);

        // 2. Xác định ngày bắt đầu và kết thúc từ kỳ lương
        Period period = payroll.getPayrollPeriod();
        if (period == null) {
            throw new IllegalArgumentException("Kỳ lương không tồn tại");
        }
        LocalDate start = period.getFromDate();
        LocalDate end = period.getToDate();
        if (start == null || end == null) {
            int month = period.getMonth() != null ? period.getMonth() : 1;
            int year = period.getYear() != null ? period.getYear() : 2026;
            start = LocalDate.of(year, month, 1);
            end = start.withDayOfMonth(start.lengthOfMonth());
        }

        // 3. Lấy danh sách nhân viên không bị xóa
        List<Staff> staffs = staffRepository.findActiveStaffs();
        List<Payslip> payslips = new ArrayList<>();

        double standardWorkDays = period.getStandardWorkDays() != null ? period.getStandardWorkDays() : 26.0;

        for (Staff staff : staffs) {
            // Lấy các khoản cấu hình lương của nhân viên trước để kiểm tra
            List<StaffSalaryItem> staffSalaryItems = staffSalaryItemRepository.findByStaffId(staff.getId());
            if (staffSalaryItems == null || staffSalaryItems.isEmpty()) {
                continue; // Chỉ tính phiếu lương cho nhân viên có cấu hình lương
            }

            // Gọi PayslipService để tính toán chi tiết phiếu lương cho nhân viên này
            Payslip payslip = payslipService.calculateStaffPayslip(staff, payroll, start, end, standardWorkDays, staffSalaryItems);
            payslips.add(payslip);
        }

        return payslipRepository.saveAll(payslips);
    }

    @Override
    public List<Payslip> getPayrollDetails(UUID payrollId) {
        Specification<Payslip> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("payroll").get("id"), payrollId),
                cb.or(cb.isNull(root.get("voided")), cb.equal(root.get("voided"), false)));
        return payslipRepository.findAll(spec);
    }

    @Override
    public Payroll confirmPayroll(UUID payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new IllegalArgumentException("Bảng lương không tồn tại"));
        payroll.setStatus(PayrollStatus.CONFIRMED);
        return payrollRepository.save(payroll);
    }

    @Override
    @Transactional
    public void deletePayroll(UUID payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new IllegalArgumentException("Bảng lương không tồn tại"));
        payroll.setVoided(true);
        payrollRepository.save(payroll);

        List<Payslip> payslips = payslipRepository.findByPayrollId(payrollId);
        for (Payslip payslip : payslips) {
            payslip.setVoided(true);
        }
        payslipRepository.saveAll(payslips);
    }

    @Override
    public Payslip getMyPayslip(UUID periodId, User currentUser) {
        if (currentUser == null || currentUser.getStaff() == null) {
            throw new IllegalArgumentException("Người dùng hiện tại chưa liên kết với nhân viên nào");
        }
        return payslipRepository
                .findByStaffIdAndPayrollPayrollPeriodIdAndPayrollStatus(currentUser.getStaff().getId(), periodId,
                        PayrollStatus.CONFIRMED)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu lương đã khóa cho kỳ lương này"));
    }

    @Override
    public Payslip updatePayslip(UUID id, com.tlu.hrm.enums.PaidStatus paidStatus, String note) {
        Payslip payslip = payslipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Phiếu lương không tồn tại"));
        payslip.setPaidStatus(paidStatus);
        payslip.setNote(note);
        return payslipRepository.save(payslip);
    }
}
