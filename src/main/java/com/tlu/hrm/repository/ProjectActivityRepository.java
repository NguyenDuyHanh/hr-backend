package com.tlu.hrm.repository;

import com.tlu.hrm.model.ProjectActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectActivityRepository extends JpaRepository<ProjectActivity, UUID> {
    List<ProjectActivity> findByProjectId(UUID projectId);
    List<ProjectActivity> findByProjectIdOrderByDisplayOrderAsc(UUID projectId);
}
