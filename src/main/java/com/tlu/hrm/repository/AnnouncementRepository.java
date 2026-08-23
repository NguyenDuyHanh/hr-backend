package com.tlu.hrm.repository;

import com.tlu.hrm.model.Announcement;
import com.tlu.hrm.enums.AnnouncementCategory;
import com.tlu.hrm.enums.AnnouncementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {
    @Query("SELECT a FROM Announcement a WHERE a.isDeleted = false " +
           "AND (CAST(:keyword AS string) IS NULL " +
           "OR LOWER(a.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) " +
           "OR LOWER(a.content) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) " +
           "OR LOWER(a.code) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) " +
           "AND (:category IS NULL OR a.category = :category) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:deptId IS NULL OR a.targetDeptId IS NULL OR a.targetDeptId = :deptId) " +
           "ORDER BY a.publishDate DESC, a.createDate DESC")
    Page<Announcement> searchAnnouncements(
            @Param("keyword") String keyword,
            @Param("category") AnnouncementCategory category,
            @Param("status") AnnouncementStatus status,
            @Param("deptId") UUID deptId,
            Pageable pageable);

    @Query("SELECT MAX(a.code) FROM Announcement a WHERE a.code LIKE :prefix%")
    String findMaxCodeByPrefix(@Param("prefix") String prefix);
}
