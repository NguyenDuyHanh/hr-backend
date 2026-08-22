package com.tlu.hrm.dto.search;

import com.tlu.hrm.enums.AnnouncementCategory;
import com.tlu.hrm.enums.AnnouncementStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementSearchRequest extends SearchDto {
    private AnnouncementCategory category;
    private AnnouncementStatus status;
    private UUID targetDeptId;
}
