package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.Bank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankDto {
    private UUID id;
    private String code;
    private String name;
    private String shortName;
    private String bin;
    private String logo;
    private String swiftCode;
    private Boolean transferSupported;
    private Boolean lookupSupported;

    public BankDto(Bank entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.code = entity.getCode();
            this.name = entity.getName();
            this.shortName = entity.getShortName();
            this.bin = entity.getBin();
            this.logo = entity.getLogo();
            this.swiftCode = entity.getSwiftCode();
            this.transferSupported = entity.getTransferSupported();
            this.lookupSupported = entity.getLookupSupported();
        }
    }
}
