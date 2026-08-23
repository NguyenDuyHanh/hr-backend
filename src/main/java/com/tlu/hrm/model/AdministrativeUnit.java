package com.tlu.hrm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_administrative_unit", uniqueConstraints = {
    @UniqueConstraint(name = "uk_admin_unit_code_level", columnNames = {"code", "level"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeUnit extends BaseModel {

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "codename")
    private String codename;

    @Column(name = "division_type")
    private String divisionType;

    @Column(name = "short_codename")
    private String shortCodename;

    @Column(name = "phone_code")
    private String phoneCode;

    @Column(name = "level")
    private Integer level;

    @Column(name = "parent_code")
    private String parentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private AdministrativeUnit parent;
}
