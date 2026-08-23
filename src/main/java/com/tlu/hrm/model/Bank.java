package com.tlu.hrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_bank")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bank extends BaseModel {

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "bin")
    private String bin;

    @Column(name = "logo")
    private String logo;

    @Column(name = "swift_code")
    private String swiftCode;

    @Column(name = "transfer_supported")
    private Boolean transferSupported = true;

    @Column(name = "lookup_supported")
    private Boolean lookupSupported = true;
}
