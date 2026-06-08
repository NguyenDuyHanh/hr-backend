package com.tlu.hrm.repository;

import com.tlu.hrm.model.ProjectStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectStaffRepository extends JpaRepository<ProjectStaff, UUID> {
    List<ProjectStaff> findByProjectId(UUID projectId);
    List<ProjectStaff> findByStaffId(UUID staffId);
    void deleteByProjectIdAndStaffId(UUID projectId, UUID staffId);
}
