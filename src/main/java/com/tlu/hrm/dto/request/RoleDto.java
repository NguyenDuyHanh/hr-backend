package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createDate;

    public RoleDto(Role entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.name = entity.getName();
            this.description = entity.getDescription();
            this.createDate = entity.getCreateDate();
        }
    }
}
