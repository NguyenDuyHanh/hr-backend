package com.tlu.hrm.repository;

import com.tlu.hrm.enums.LeaveApprovalStatus;
import com.tlu.hrm.model.LeaveRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID>, JpaSpecificationExecutor<LeaveRequest> {

    @Query("SELECT COALESCE(SUM(r.totalDays), 0.0) FROM LeaveRequest r WHERE r.requestStaff.id = :staffId " +
           "AND r.leaveType = 'ANNUAL' AND r.approvalStatus = 'APPROVED' " +
           "AND r.isDeleted = false AND EXTRACT(YEAR FROM r.fromDate) = :year")
    Double calculateUsedAnnualLeave(@Param("staffId") UUID staffId, @Param("year") int year);

    @Query("SELECT r FROM LeaveRequest r WHERE r.requestStaff.id = :staffId " +
           "AND (:excludeId IS NULL OR r.id != :excludeId) " +
           "AND r.approvalStatus != 'REJECTED' " +
           "AND r.isDeleted = false " +
           "AND r.fromDate <= :toDate AND r.toDate >= :fromDate")
    List<LeaveRequest> findOverlappingRequests(
            @Param("staffId") UUID staffId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("excludeId") UUID excludeId);

    @Query("SELECT r FROM LeaveRequest r WHERE r.requestStaff.id = :staffId " +
           "AND r.approvalStatus = 'APPROVED' " +
           "AND r.isDeleted = false " +
           "AND :date BETWEEN r.fromDate AND r.toDate")
    List<LeaveRequest> findApprovedLeaveRequestsOnDate(
            @Param("staffId") UUID staffId,
            @Param("date") LocalDate date);

    @Query("SELECT COUNT(r) FROM LeaveRequest r WHERE r.approvalStatus = :status AND (r.isDeleted = false OR r.isDeleted IS NULL)")
    long countByApprovalStatusAndNotDeleted(@Param("status") LeaveApprovalStatus status);

    @Query("SELECT r FROM LeaveRequest r WHERE (r.isDeleted = false OR r.isDeleted IS NULL) ORDER BY r.createDate DESC")
    List<LeaveRequest> findRecentLeaves(Pageable pageable);
}
