package com.tlu.hrm.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum EducationDegree {
    DOCTORATE("Tiến sĩ"),
    MASTER("Thạc sĩ"),
    BACHELOR("Đại học"),
    ASSOCIATE("Cao đẳng"),
    INTERMEDIATE("Trung cấp"),
    HIGH_SCHOOL("Trung học phổ thông"),
    OTHER("Khác");

    private final String description;

    EducationDegree(String description) {
        this.description = description;
    }

    @JsonCreator
    public static EducationDegree fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String upper = value.trim().toUpperCase();
        for (EducationDegree degree : EducationDegree.values()) {
            if (degree.name().equalsIgnoreCase(upper)) {
                return degree;
            }
        }
        if (upper.contains("ĐẠI HỌC") || upper.contains("DAI HOC") || upper.contains("??I H") || upper.contains("ĐẠI")) {
            return BACHELOR;
        }
        if (upper.contains("THẠC SĨ") || upper.contains("THAC SI")) {
            return MASTER;
        }
        if (upper.contains("TIẾN SĨ") || upper.contains("TIEN SI")) {
            return DOCTORATE;
        }
        if (upper.contains("CAO ĐẲNG") || upper.contains("CAO DANG")) {
            return ASSOCIATE;
        }
        if (upper.contains("TRUNG CẤP") || upper.contains("TRUNG CAP")) {
            return INTERMEDIATE;
        }
        if (upper.contains("THPT") || upper.contains("TRUNG HỌC PHỔ THÔNG")) {
            return HIGH_SCHOOL;
        }
        return OTHER;
    }
}
