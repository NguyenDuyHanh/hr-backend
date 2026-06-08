package com.tlu.hrm.security;

import com.tlu.hrm.enums.RoleType;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    public boolean isManagerOrAdmin(User user) {
        if (user == null) {
            return false;
        }
        return user.getUserRoles().stream()
                .anyMatch(ur -> RoleType.ROLE_ADMIN.getName().equals(ur.getRole().getName())
                        || RoleType.HR_MANAGER.getName().equals(ur.getRole().getName()));
    }
}
