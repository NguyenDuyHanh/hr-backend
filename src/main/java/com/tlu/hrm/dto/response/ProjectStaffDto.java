package com.tlu.hrm.dto.response;

import com.tlu.hrm.enums.ProjectRole;
import com.tlu.hrm.model.ProjectStaff;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ProjectStaffDto {
    private UUID id;
    private UUID staffId;
    private String staffCode;
    private String displayName;
    private String email;
    private ProjectRole projectRole;
    private LocalDate joinedDate;

    public ProjectStaffDto() {}

    public ProjectStaffDto(ProjectStaff entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.projectRole = entity.getProjectRole();
            this.joinedDate = entity.getJoinedDate();
            if (entity.getStaff() != null) {
                this.staffId = entity.getStaff().getId();
                this.staffCode = entity.getStaff().getStaffCode();
                this.displayName = entity.getStaff().getDisplayName();
                this.email = entity.getStaff().getEmail();
            }
        }
    }
}
