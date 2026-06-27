package com.tlu.hrm.dto.search;

import com.tlu.hrm.enums.RecruitmentStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SearchRecruitmentDto extends SearchDto {
    private RecruitmentStatus status;
    private List<UUID> chosenIds;
    
    // New search filters
    private String code;
    private String name;
    private UUID personApproveCVId;
}
