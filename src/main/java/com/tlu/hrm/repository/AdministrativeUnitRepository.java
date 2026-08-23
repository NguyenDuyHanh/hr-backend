package com.tlu.hrm.repository;

import com.tlu.hrm.model.AdministrativeUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdministrativeUnitRepository extends JpaRepository<AdministrativeUnit, UUID> {
    Optional<AdministrativeUnit> findByCode(String code);
    Optional<AdministrativeUnit> findByCodeAndLevel(String code, Integer level);
    List<AdministrativeUnit> findByLevelAndIsDeletedFalseOrderByNameAsc(Integer level);
    List<AdministrativeUnit> findByParentCodeAndIsDeletedFalseOrderByNameAsc(String parentCode);

    @Query("SELECT u FROM AdministrativeUnit u WHERE u.isDeleted = false " +
           "AND (:level IS NULL OR u.level = :level) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<AdministrativeUnit> searchUnits(@Param("keyword") String keyword, @Param("level") Integer level, Pageable pageable);
}
