package com.tlu.hrm.utils;

import com.tlu.hrm.model.Project;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.Task;
import com.tlu.hrm.model.User;
import com.tlu.hrm.enums.ProjectRole;
import com.tlu.hrm.repository.ProjectRepository;
import com.tlu.hrm.repository.TaskRepository;
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

    @Autowired
    private TaskRepository taskRepository;

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
                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                .orElse(null);
        if (project == null) {
            return false;
        }
        Staff staff = user.getStaff();
        if (staff == null) {
            return false;
        }
        return project.getProjectStaffs().stream()
                .filter(ps -> ps.getIsDeleted() == null || !ps.getIsDeleted())
                .anyMatch(ps -> ps.getStaff() != null
                        && ps.getStaff().getId().equals(staff.getId())
                        && ProjectRole.MANAGER.equals(ps.getProjectRole()));
    }

    /**
     * Checks if the current authenticated user has general view/access to the
     * project
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
                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                .orElse(null);
        if (project == null) {
            return false;
        }
        Staff staff = user.getStaff();
        if (staff == null) {
            return false;
        }
        return project.getProjectStaffs().stream()
                .filter(ps -> ps.getIsDeleted() == null || !ps.getIsDeleted())
                .anyMatch(ps -> ps.getStaff() != null
                        && ps.getStaff().getId().equals(staff.getId()));
    }

    /**
     * Checks if the current authenticated user has manager access to the project
     * associated with the given task ID.
     */
    public boolean hasProjectManagerAccessByTaskId(UUID taskId) {
        if (taskId == null) {
            return false;
        }
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getIsDeleted() == null || !t.getIsDeleted())
                .orElse(null);
        if (task == null || task.getProject() == null) {
            return false;
        }
        return hasProjectManagerAccess(task.getProject().getId());
    }

    /**
     * Checks if the current authenticated user has general view/access to the
     * project
     * associated with the given task ID.
     */
    public boolean hasProjectAccessByTaskId(UUID taskId) {
        if (taskId == null) {
            return false;
        }
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getIsDeleted() == null || !t.getIsDeleted())
                .orElse(null);
        if (task == null || task.getProject() == null) {
            return false;
        }
        return hasProjectAccess(task.getProject().getId());
    }
}
