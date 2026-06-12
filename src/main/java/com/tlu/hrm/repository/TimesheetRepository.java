package com.tlu.hrm.repository;

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
}
