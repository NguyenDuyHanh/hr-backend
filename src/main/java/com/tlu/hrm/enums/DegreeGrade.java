package com.tlu.hrm.enums;

import lombok.Getter;

@Getter
public enum DegreeGrade {
    EXCELLENT("Xuất sắc"),
    GOOD("Giỏi"),
    FAIR("Khá"),
    AVERAGE_GOOD("Trung bình khá"),
    AVERAGE("Trung bình");

    private final String description;

    DegreeGrade(String description) {
        this.description = description;
    }
}
