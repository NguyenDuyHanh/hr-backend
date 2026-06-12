package com.tlu.hrm.repository;

import com.tlu.hrm.model.ShiftWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftWorkRepository extends JpaRepository<ShiftWork, UUID> {
    Optional<ShiftWork> findByCode(String code);
}
