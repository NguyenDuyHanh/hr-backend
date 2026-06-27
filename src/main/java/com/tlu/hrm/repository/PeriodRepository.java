package com.tlu.hrm.repository;

import com.tlu.hrm.model.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PeriodRepository extends JpaRepository<Period, UUID>, JpaSpecificationExecutor<Period> {
    Optional<Period> findByCode(String code);

    @Query("SELECT p FROM Period p WHERE (p.isDeleted IS NULL OR p.isDeleted = false) AND p.code = :code")
    Optional<Period> findActiveByCode(@Param("code") String code);

    @Query("SELECT p FROM Period p WHERE :date BETWEEN p.fromDate AND p.toDate AND (p.isDeleted = false OR p.isDeleted IS NULL)")
    Optional<Period> findPeriodContainingDate(@Param("date") LocalDate date);
}

