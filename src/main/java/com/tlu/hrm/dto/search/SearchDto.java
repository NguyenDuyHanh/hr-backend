package com.tlu.hrm.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchDto implements Serializable {

    private Long id;
    private int pageIndex;
    private int pageSize;
    private String keyword;
    private Boolean isDeleted;
    private String orderBy;
    private String extWhereClause;
    private String orderByDesc;
    private UUID departmentId;
    private Boolean active;
    private UUID positionId;
    private UUID roleId;
}
