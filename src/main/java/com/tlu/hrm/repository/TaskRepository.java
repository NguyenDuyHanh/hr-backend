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
    
    List<Task> findByProjectId(UUID projectId);

    List<Task> findByProjectIdAndIsDeletedFalse(UUID projectId);
    
    List<Task> findByProjectIdAndStatusIdAndIsDeletedFalse(UUID projectId, UUID statusId);
}
