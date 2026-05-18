package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(
        name = "create_date",
        nullable = true
    )
    private LocalDateTime createDate;

    @Column(
        name = "created_by",
        length = 100,
        nullable = true
    )
    private String createdBy;

    @Column(
        name = "modify_date",
        nullable = true
    )
    private LocalDateTime modifyDate;

    @Column(
        name = "modified_by",
        length = 100,
        nullable = true
    )
    private String modifiedBy;

    @Column(
        name = "voided"
    )
    private Boolean voided = false;
}
