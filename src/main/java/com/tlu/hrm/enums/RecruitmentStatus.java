package com.tlu.hrm.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RecruitmentStatus {
    DRAFT(0, "Nháp"),
    RECRUITING(1, "Đang tuyển"),
    PAUSED(2, "Tạm dừng"),
    CLOSED(3, "Đã đóng");

    @JsonValue
    private final int value;
    private final String label;

    RecruitmentStatus(int value, String label) {
        this.value = value;
        this.label = label;
    }

    @JsonCreator
    public static RecruitmentStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (RecruitmentStatus status : RecruitmentStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return null;
    }
}
