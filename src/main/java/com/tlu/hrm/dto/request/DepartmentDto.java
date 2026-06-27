package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime createDate;

    public DepartmentDto(Department entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.code = entity.getCode();
            this.name = entity.getName();
            this.description = entity.getDescription();
            this.createDate = entity.getCreateDate();
        }
    }
}
