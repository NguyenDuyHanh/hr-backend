package com.tlu.hrm.utils;

import com.tlu.hrm.model.Project;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.User;
import com.tlu.hrm.enums.ProjectRole;
import com.tlu.hrm.repository.ProjectRepository;
import com.tlu.hrm.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProjectUtils {

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Checks if the current authenticated user has manager access to the project
     * (assigned as MANAGER of this project).
     */
    public boolean hasProjectManagerAccess(UUID projectId) {
        if (projectId == null) {
            return false;
        }
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            return false;
        }
        Project project = projectRepository.findById(projectId)
                .filter(p -> p.getVoided() == null || !p.getVoided())
                .orElse(null);
        if (project == null) {
            return false;
        }
        Staff staff = user.getStaff();
        if (staff == null) {
            return false;
        }
        return project.getProjectStaffs().stream()
                .filter(ps -> ps.getVoided() == null || !ps.getVoided())
                .anyMatch(ps -> ps.getStaff() != null 
                        && ps.getStaff().getId().equals(staff.getId())
                        && ProjectRole.MANAGER.equals(ps.getProjectRole()));
    }

    /**
     * Checks if the current authenticated user has general view/access to the project
     * (assigned as any role of this project).
     */
    public boolean hasProjectAccess(UUID projectId) {
        if (projectId == null) {
            return false;
        }
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            return false;
        }
        Project project = projectRepository.findById(projectId)
                .filter(p -> p.getVoided() == null || !p.getVoided())
                .orElse(null);
        if (project == null) {
            return false;
        }
        Staff staff = user.getStaff();
        if (staff == null) {
            return false;
        }
        return project.getProjectStaffs().stream()
                .filter(ps -> ps.getVoided() == null || !ps.getVoided())
                .anyMatch(ps -> ps.getStaff() != null 
                        && ps.getStaff().getId().equals(staff.getId()));
    }
}
