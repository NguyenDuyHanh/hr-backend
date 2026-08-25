package com.tlu.hrm.enums;

import lombok.Getter;

@Getter
public enum DegreeLevel {
    BACHELOR("Cử nhân"),
    ENGINEER("Kỹ sư"),
    MASTER("Thạc sĩ"),
    DOCTORATE("Tiến sĩ"),
    ASSOCIATE("Cao đẳng"),
    INTERMEDIATE("Trung cấp"),
    OTHER("Khác");

    private final String description;

    DegreeLevel(String description) {
        this.description = description;
    }
}
