package com.tlu.hrm.dto.request;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tlu.hrm.model.Role;
import com.tlu.hrm.model.User;
import com.tlu.hrm.model.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String username;
    private String password;
    private Boolean active;
    private Set<Role> roles;
    private UUID staffId;
    private String staffName;
    private String staffCode;
    private String avatarUrl;
    private String email;

    public UserDto(User entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.username = entity.getUsername();
            this.active = entity.getActive();
            if (entity.getUserRoles() != null) {
                this.roles = entity.getUserRoles().stream().map(UserRole::getRole).collect(Collectors.toSet());
            }
            if (entity.getStaff() != null) {
                this.staffId = entity.getStaff().getId();
                this.staffName = entity.getStaff().getDisplayName();
                this.staffCode = entity.getStaff().getStaffCode();
                this.avatarUrl = entity.getStaff().getAvatarUrl();
                this.email = entity.getStaff().getEmail();
            }
        }
    }
}
