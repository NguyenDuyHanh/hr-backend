package com.tlu.hrm.repository;

import com.tlu.hrm.model.PayslipItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PayslipItemRepository extends JpaRepository<PayslipItem, UUID>, JpaSpecificationExecutor<PayslipItem> {
}
