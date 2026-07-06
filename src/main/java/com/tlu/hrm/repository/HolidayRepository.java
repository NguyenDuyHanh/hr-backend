package com.tlu.hrm.repository;

import com.tlu.hrm.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, UUID> {

    @Query("SELECT h FROM Holiday h WHERE (h.isDeleted = false OR h.isDeleted IS NULL)")
    List<Holiday> findAllActive();

    @Query("SELECT h FROM Holiday h WHERE h.startDate <= :date AND h.endDate >= :date AND (h.isDeleted = false OR h.isDeleted IS NULL)")
    List<Holiday> findHolidaysContainingDate(@Param("date") LocalDate date);

    @Query("SELECT h FROM Holiday h WHERE h.year = :year AND (h.isDeleted = false OR h.isDeleted IS NULL)")
    List<Holiday> findByYear(@Param("year") Integer year);

    @Query("SELECT h FROM Holiday h WHERE h.startDate <= :endDate AND h.endDate >= :startDate AND (h.isDeleted = false OR h.isDeleted IS NULL)")
    List<Holiday> findHolidaysInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT h FROM Holiday h WHERE (:id IS NULL OR h.id != :id) AND h.code = :code AND (h.isDeleted = false OR h.isDeleted IS NULL)")
    Optional<Holiday> findActiveByCode(@Param("code") String code, @Param("id") UUID id);

    @Query("SELECT h FROM Holiday h WHERE (:id IS NULL OR h.id != :id) AND h.startDate <= :endDate AND h.endDate >= :startDate AND (h.isDeleted = false OR h.isDeleted IS NULL)")
    List<Holiday> findOverlappingHolidays(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("id") UUID id);

    Optional<Holiday> findByCode(String code);
}
