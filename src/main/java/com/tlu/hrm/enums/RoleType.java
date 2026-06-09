package com.tlu.hrm.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    ROLE_ADMIN(Constants.ROLE_ADMIN, "Quản trị viên hệ thống"),
    HR_MANAGER(Constants.HR_MANAGER, "Quản lý nhân sự"),
    HR_EMPLOYEE(Constants.HR_EMPLOYEE, "Nhân viên"),
    HR_RECRUITMENT(Constants.HR_RECRUITMENT, "Quản lý tuyển dụng"),
    HR_COMPENSATION_BENEFIT(Constants.HR_COMPENSATION_BENEFIT, "Quản lý lương và chế độ đãi ngộ"),
    HR_TIMEKEEPING_MANAGER(Constants.HR_TIMEKEEPING_MANAGER, "Quản lý chấm công");

    private final String name;
    private final String description;

    RoleType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static class Constants {
        public static final String ROLE_ADMIN = "ROLE_ADMIN";
        public static final String HR_MANAGER = "HR_MANAGER";
        public static final String HR_EMPLOYEE = "HR_EMPLOYEE";
        public static final String HR_RECRUITMENT = "HR_RECRUITMENT";
        public static final String HR_COMPENSATION_BENEFIT = "HR_COMPENSATION_BENEFIT";
        public static final String HR_TIMEKEEPING_MANAGER = "HR_TIMEKEEPING_MANAGER";
    }
}
