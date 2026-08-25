package com.tlu.hrm.dto.response;

import com.tlu.hrm.model.SalaryItem;
import com.tlu.hrm.model.StaffSalaryItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffSalaryItemResponse {
    private UUID id;
    private UUID staffId;
    private String staffCode;
    private String staffDisplayName;
    private SalaryItem salaryItem;
    private Double amount;

    public StaffSalaryItemResponse(StaffSalaryItem entity) {
        if (entity != null) {
            this.id = entity.getId();
            if (entity.getStaff() != null) {
                this.staffId = entity.getStaff().getId();
                this.staffCode = entity.getStaff().getStaffCode();
                this.staffDisplayName = entity.getStaff().getDisplayName();
            }
            this.salaryItem = entity.getSalaryItem();
            this.amount = entity.getAmount();
        }
    }
}
