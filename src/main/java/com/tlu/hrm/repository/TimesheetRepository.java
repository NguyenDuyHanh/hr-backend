package com.tlu.hrm.repository;

import com.tlu.hrm.enums.TimesheetStatus;
import com.tlu.hrm.model.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, UUID>, JpaSpecificationExecutor<Timesheet> {
    Optional<Timesheet> findByStaffIdAndWorkingDate(UUID staffId, LocalDate workingDate);
    List<Timesheet> findByStaffIdAndWorkingDateBetweenOrderByWorkingDateAsc(UUID staffId, LocalDate start, LocalDate end);
    List<Timesheet> findByStaffIdAndWorkingDateBetweenAndStatus(UUID staffId, LocalDate start, LocalDate end, TimesheetStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Timesheet t WHERE t.staff.id = :staffId AND t.workingDate BETWEEN :start AND :end AND t.status = :status AND (t.voided = false OR t.voided IS NULL)")
    List<Timesheet> findActiveTimesheetsByStaffIdAndWorkingDateBetweenAndStatus(
            @org.springframework.data.repository.query.Param("staffId") UUID staffId, 
            @org.springframework.data.repository.query.Param("start") java.time.LocalDate start, 
            @org.springframework.data.repository.query.Param("end") java.time.LocalDate end, 
            @org.springframework.data.repository.query.Param("status") TimesheetStatus status);
}

