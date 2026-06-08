package com.tlu.hrm.enums;

import lombok.Getter;

@Getter
public enum ProjectRole {
    MANAGER("Quản lý dự án"),
    MEMBER("Người thực hiện");

    private final String description;

    ProjectRole(String description) {
        this.description = description;
    }
}

