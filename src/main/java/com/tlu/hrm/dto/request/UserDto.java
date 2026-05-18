package com.tlu.hrm.dto.request;

import com.tlu.hrm.model.Role;
import com.tlu.hrm.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String username;
    private String password;
    private String email;
    private Boolean active;
    private Set<Role> roles;
    private UUID staffId;
    private String staffName;

    public UserDto(User entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.username = entity.getUsername();
            this.password = entity.getPassword();
            this.email = entity.getEmail();
            this.active = entity.getActive();
            this.roles = entity.getRoles();
            if (entity.getStaff() != null) {
                this.staffId = entity.getStaff().getId();
                this.staffName = entity.getStaff().getDisplayName();
            }
        }
    }
}
