package com.tlu.hrm.repository;

import com.tlu.hrm.model.ProjectWorkingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectWorkingStatusRepository extends JpaRepository<ProjectWorkingStatus, UUID> {
    List<ProjectWorkingStatus> findByProjectId(UUID projectId);
    List<ProjectWorkingStatus> findByProjectIdOrderByDisplayOrderAsc(UUID projectId);
}
