package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.AdministrativeUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeUnitDto {
    private UUID id;
    private String code;
    private String name;
    private String codename;
    private String divisionType;
    private String shortCodename;
    private String phoneCode;
    private Integer level;
    private String parentCode;
    private UUID parentId;
    private String parentName;

    public AdministrativeUnitDto(AdministrativeUnit entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.code = entity.getCode();
            this.name = entity.getName();
            this.codename = entity.getCodename();
            this.divisionType = entity.getDivisionType();
            this.shortCodename = entity.getShortCodename();
            this.phoneCode = entity.getPhoneCode();
            this.level = entity.getLevel();
            this.parentCode = entity.getParentCode();
            if (entity.getParent() != null) {
                this.parentId = entity.getParent().getId();
                this.parentName = entity.getParent().getName();
            }
        }
    }
}
