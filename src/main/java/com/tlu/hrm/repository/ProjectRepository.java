package com.tlu.hrm.repository;

import com.tlu.hrm.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findByCode(String code);
    
    @Query("SELECT p.code FROM Project p WHERE p.code LIKE 'PROJ%'")
    List<String> findMaxProjectCodes();
}
