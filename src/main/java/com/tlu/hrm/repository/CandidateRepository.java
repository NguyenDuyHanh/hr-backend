package com.tlu.hrm.repository;

import com.tlu.hrm.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, UUID> {
    boolean existsByCandidateCodeAndIdNot(String candidateCode, UUID id);
    boolean existsByCandidateCode(String candidateCode);
    long countByRecruitmentId(UUID recruitmentId);
}
