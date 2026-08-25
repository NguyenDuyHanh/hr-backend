package com.tlu.hrm.repository;

import com.tlu.hrm.model.StaffBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffBankAccountRepository extends JpaRepository<StaffBankAccount, UUID> {
    List<StaffBankAccount> findByStaffIdAndIsDeletedFalse(UUID staffId);
    Optional<StaffBankAccount> findByStaffIdAndIsDefaultTrueAndIsDeletedFalse(UUID staffId);
}
