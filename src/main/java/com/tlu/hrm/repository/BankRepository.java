package com.tlu.hrm.repository;

import com.tlu.hrm.model.Bank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankRepository extends JpaRepository<Bank, UUID> {
    Optional<Bank> findByCode(String code);
    List<Bank> findByIsDeletedFalseOrderByNameAsc();
    Page<Bank> findByIsDeletedFalseAndNameContainingIgnoreCaseOrIsDeletedFalseAndCodeContainingIgnoreCaseOrIsDeletedFalseAndShortNameContainingIgnoreCase(
            String nameQuery, String codeQuery, String shortNameQuery, Pageable pageable);
}
