package com.tlu.hrm.repository;

import com.tlu.hrm.model.SalaryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalaryItemRepository extends JpaRepository<SalaryItem, UUID>, JpaSpecificationExecutor<SalaryItem> {
    Optional<SalaryItem> findByCode(String code);

    @Query("SELECT s FROM SalaryItem s WHERE (s.isDeleted IS NULL OR s.isDeleted = false) AND s.code = :code")
    Optional<SalaryItem> findActiveByCode(@Param("code") String code);
}
