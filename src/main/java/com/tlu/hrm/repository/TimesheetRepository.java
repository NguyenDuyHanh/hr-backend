package com.tlu.hrm.repository;

import com.tlu.hrm.enums.TimesheetStatus;
import com.tlu.hrm.model.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, UUID>, JpaSpecificationExecutor<Timesheet> {
    Optional<Timesheet> findByStaffIdAndWorkingDate(UUID staffId, LocalDate workingDate);

    List<Timesheet> findByStaffIdAndWorkingDateBetweenOrderByWorkingDateAsc(UUID staffId, LocalDate start,
            LocalDate end);

    List<Timesheet> findByStaffIdAndWorkingDateBetweenAndStatus(UUID staffId, LocalDate start, LocalDate end,
            TimesheetStatus status);

    @Query("SELECT t FROM Timesheet t WHERE t.staff.id = :staffId AND t.workingDate BETWEEN :start AND :end AND t.status = :status AND (t.isDeleted = false OR t.isDeleted IS NULL)")
    List<Timesheet> findActiveTimesheetsByStaffIdAndWorkingDateBetweenAndStatus(
            @Param("staffId") UUID staffId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("status") TimesheetStatus status);

    @Query("SELECT COUNT(t) FROM Timesheet t WHERE t.workingDate = :date AND t.status = :status AND (t.isDeleted = false OR t.isDeleted IS NULL)")
    long countByWorkingDateAndStatusAndNotDeleted(@Param("date") LocalDate date,
            @Param("status") TimesheetStatus status);
}
