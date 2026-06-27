package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionDto {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private UUID departmentId;
    private String departmentName;
    private LocalDateTime createDate;

    public PositionDto(Position entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.code = entity.getCode();
            this.name = entity.getName();
            this.description = entity.getDescription();
            this.createDate = entity.getCreateDate();
            if (entity.getDepartment() != null) {
                this.departmentId = entity.getDepartment().getId();
                this.departmentName = entity.getDepartment().getName();
            }
        }
    }
}
