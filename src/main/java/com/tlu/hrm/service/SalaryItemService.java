package com.tlu.hrm.service;

import com.tlu.hrm.model.SalaryItem;
import com.tlu.hrm.model.StaffSalaryItem;

import java.util.List;
import java.util.UUID;

public interface SalaryItemService {
    List<SalaryItem> getAllSalaryItems();
    SalaryItem saveSalaryItem(SalaryItem item);
    void deleteSalaryItem(UUID id);
    List<StaffSalaryItem> getStaffSalaryItems(UUID staffId);
    List<StaffSalaryItem> saveStaffSalaryItems(UUID staffId, List<StaffSalaryItem> items);
}
