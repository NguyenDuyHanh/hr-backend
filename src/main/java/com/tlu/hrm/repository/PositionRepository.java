package com.tlu.hrm.repository;

import com.tlu.hrm.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, UUID> {
    Optional<Position> findByCode(String code);

    @Query("SELECT COUNT(p) > 0 FROM Position p WHERE p.department.id = :deptId AND (p.isDeleted IS NULL OR p.isDeleted = false)")
    boolean existsActivePositionsByDepartmentId(@Param("deptId") UUID deptId);
}
