package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.Ethnic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EthnicDto {
    private UUID id;
    private String code;
    private String name;
    private String description;

    public EthnicDto(Ethnic entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.code = entity.getCode();
            this.name = entity.getName();
            this.description = entity.getDescription();
        }
    }
}
