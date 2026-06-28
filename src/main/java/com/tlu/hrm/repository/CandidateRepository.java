package com.tlu.hrm.repository;

import com.tlu.hrm.enums.CandidateStatus;
import com.tlu.hrm.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, UUID> {
    boolean existsByCandidateCodeAndIdNot(String candidateCode, UUID id);
    boolean existsByCandidateCode(String candidateCode);
    long countByRecruitmentId(UUID recruitmentId);

    @Query("SELECT COUNT(c) FROM Candidate c WHERE c.status = :status AND (c.isDeleted = false OR c.isDeleted IS NULL)")
    long countByStatusAndNotDeleted(@Param("status") CandidateStatus status);
}
