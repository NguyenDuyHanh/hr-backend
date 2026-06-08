package com.tlu.hrm.dto.request;

import com.tlu.hrm.enums.ProjectRole;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ProjectStaffRequest {
    private UUID staffId;
    private ProjectRole projectRole;
    private LocalDate joinedDate;
}
