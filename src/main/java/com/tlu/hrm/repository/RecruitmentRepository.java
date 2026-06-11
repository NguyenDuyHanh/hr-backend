package com.tlu.hrm.repository;

import com.tlu.hrm.model.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, UUID> {
    boolean existsByCodeAndIdNot(String code, UUID id);
    boolean existsByCode(String code);
}
