package com.tlu.hrm.repository;

import com.tlu.hrm.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    @Query("SELECT MAX(t.code) FROM Task t WHERE t.project.id = :projectId")
    Long findMaxCodeByProjectId(@Param("projectId") UUID projectId);

    List<Task> findByProjectIdAndVoidedFalse(UUID projectId);
    
    List<Task> findByProjectIdAndStatusIdAndVoidedFalse(UUID projectId, UUID statusId);
}
