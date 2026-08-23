package com.tlu.hrm.repository;

import com.tlu.hrm.model.Ethnic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EthnicRepository extends JpaRepository<Ethnic, UUID> {
    Optional<Ethnic> findByCode(String code);
    List<Ethnic> findByIsDeletedFalseOrderByNameAsc();
    Page<Ethnic> findByIsDeletedFalseAndNameContainingIgnoreCaseOrIsDeletedFalseAndCodeContainingIgnoreCase(
            String nameQuery, String codeQuery, Pageable pageable);
}
