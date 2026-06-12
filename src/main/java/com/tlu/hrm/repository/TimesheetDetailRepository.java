package com.tlu.hrm.repository;

import com.tlu.hrm.model.TimesheetDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimesheetDetailRepository extends JpaRepository<TimesheetDetail, UUID> {
    List<TimesheetDetail> findByTimesheetId(UUID timesheetId);
}
