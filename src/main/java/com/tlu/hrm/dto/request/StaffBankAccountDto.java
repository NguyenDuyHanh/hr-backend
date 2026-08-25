package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.StaffBankAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffBankAccountDto {
    private UUID id;
    private UUID staffId;
    private UUID bankId;
    private String bankName;
    private String bankShortName;
    private String bankBin;
    private String bankLogo;
    private String accountNumber;
    private String accountName;
    private String branchName;
    private Boolean isDefault;
    private String note;

    public StaffBankAccountDto(StaffBankAccount entity) {
        if (entity != null) {
            this.id = entity.getId();
            if (entity.getStaff() != null) {
                this.staffId = entity.getStaff().getId();
            }
            if (entity.getBank() != null) {
                this.bankId = entity.getBank().getId();
                this.bankName = entity.getBank().getName();
                this.bankShortName = entity.getBank().getShortName();
                this.bankBin = entity.getBank().getBin();
                this.bankLogo = entity.getBank().getLogo();
            }
            this.accountNumber = entity.getAccountNumber();
            this.accountName = entity.getAccountName();
            this.branchName = entity.getBranchName();
            this.isDefault = entity.getIsDefault();
            this.note = entity.getNote();
        }
    }
}
