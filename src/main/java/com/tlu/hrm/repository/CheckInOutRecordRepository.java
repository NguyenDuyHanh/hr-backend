package com.tlu.hrm.repository;

import com.tlu.hrm.model.CheckInOutRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CheckInOutRecordRepository extends JpaRepository<CheckInOutRecord, UUID> {
    List<CheckInOutRecord> findByStaffIdAndRecordTimeBetweenOrderByRecordTimeAsc(UUID staffId, LocalDateTime start, LocalDateTime end);
}
