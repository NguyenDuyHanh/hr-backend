package com.tlu.hrm.dto.search;

import com.tlu.hrm.enums.CandidateStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SearchCandidateDto extends SearchDto {
    private UUID recruitmentId;
    private CandidateStatus status;
}
