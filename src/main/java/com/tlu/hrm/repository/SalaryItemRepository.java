package com.tlu.hrm.repository;

import com.tlu.hrm.model.SalaryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalaryItemRepository extends JpaRepository<SalaryItem, UUID>, JpaSpecificationExecutor<SalaryItem> {
    Optional<SalaryItem> findByCode(String code);
}
