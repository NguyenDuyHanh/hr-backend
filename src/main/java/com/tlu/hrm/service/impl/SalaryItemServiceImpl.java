package com.tlu.hrm.service.impl;

import com.tlu.hrm.model.SalaryItem;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.StaffSalaryItem;
import com.tlu.hrm.repository.SalaryItemRepository;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.repository.StaffSalaryItemRepository;
import com.tlu.hrm.service.SalaryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SalaryItemServiceImpl implements SalaryItemService {

    @Autowired
    private SalaryItemRepository salaryItemRepository;

    @Autowired
    private StaffSalaryItemRepository staffSalaryItemRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public List<SalaryItem> getAllSalaryItems() {
        return salaryItemRepository.findAll().stream()
                .filter(item -> item.getIsDeleted() == null || !item.getIsDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public SalaryItem saveSalaryItem(SalaryItem item) {
        if (item.getCode() != null) {
            String finalCode = item.getCode().trim().toUpperCase();
            java.util.Optional<SalaryItem> existing = salaryItemRepository.findActiveByCode(finalCode);
            if (existing.isPresent() && (item.getId() == null || !existing.get().getId().equals(item.getId()))) {
                throw new IllegalArgumentException("Mã khoản lương '" + finalCode + "' đã tồn tại");
            }
            item.setCode(finalCode);
        }

        if (item.getId() != null) {
            SalaryItem existing = salaryItemRepository.findById(item.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Khoản lương không tồn tại"));
            existing.setName(item.getName());
            existing.setCode(item.getCode());
            existing.setType(item.getType());
            existing.setCalculationType(item.getCalculationType());
            existing.setDescription(item.getDescription());
            return salaryItemRepository.save(existing);
        }
        return salaryItemRepository.save(item);
    }

    @Override
    public void deleteSalaryItem(UUID id) {
        SalaryItem item = salaryItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khoản lương không tồn tại"));
        item.setIsDeleted(true);
        salaryItemRepository.save(item);
    }

    @Override
    public List<StaffSalaryItem> getStaffSalaryItems(UUID staffId) {
        return staffSalaryItemRepository.findByStaffId(staffId).stream()
                .filter(item -> item.getIsDeleted() == null || !item.getIsDeleted())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<StaffSalaryItem> saveStaffSalaryItems(UUID staffId, List<StaffSalaryItem> items) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));

        List<StaffSalaryItem> oldItems = staffSalaryItemRepository.findByStaffId(staffId).stream()
                .filter(item -> item.getIsDeleted() == null || !item.getIsDeleted())
                .collect(Collectors.toList());
        for (StaffSalaryItem oldItem : oldItems) {
            oldItem.setIsDeleted(true);
        }
        staffSalaryItemRepository.saveAll(oldItems);

        List<StaffSalaryItem> toSave = new ArrayList<>();
        Set<UUID> seenSalaryItemIds = new HashSet<>();
        for (StaffSalaryItem item : items) {
            if (item.getSalaryItem() == null || item.getSalaryItem().getId() == null) {
                continue;
            }
            UUID salaryItemId = item.getSalaryItem().getId();
            if (seenSalaryItemIds.contains(salaryItemId)) {
                throw new IllegalArgumentException("Khoản lương cấu hình bị trùng lặp cho nhân viên này");
            }
            seenSalaryItemIds.add(salaryItemId);

            StaffSalaryItem ssi = new StaffSalaryItem();
            ssi.setStaff(staff);

            SalaryItem salaryItem = salaryItemRepository.findById(salaryItemId)
                    .orElseThrow(() -> new IllegalArgumentException("Khoản lương không hợp lệ"));
            ssi.setSalaryItem(salaryItem);
            ssi.setAmount(item.getAmount());
            toSave.add(ssi);
        }
        return staffSalaryItemRepository.saveAll(toSave);
    }
}
