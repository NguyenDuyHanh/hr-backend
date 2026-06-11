package com.tlu.hrm.dto.search;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SearchRecruitmentDto extends SearchDto {
    private Integer status;
    private List<UUID> chosenIds;
    
    // New search filters
    private String code;
    private String name;
    private UUID personApproveCVId;
}
