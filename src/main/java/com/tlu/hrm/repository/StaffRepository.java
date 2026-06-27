package com.tlu.hrm.repository;

import com.tlu.hrm.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<Staff, UUID> {
    Staff findFirstByOrderByStaffCodeDesc();

    @org.springframework.data.jpa.repository.Query("SELECT s.staffCode FROM Staff s WHERE s.staffCode LIKE 'NV%_%'")
    java.util.List<String> findMaxValidStaffCode();

    List<Staff> findByIsDeletedFalse();

    @org.springframework.data.jpa.repository.Query("SELECT s FROM Staff s WHERE s.isDeleted = false OR s.isDeleted IS NULL")
    List<Staff> findActiveStaffs();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) > 0 FROM Staff s WHERE s.email = :email AND (s.isDeleted = false OR s.isDeleted IS NULL)")
    boolean existsByEmailAndActive(@org.springframework.data.repository.query.Param("email") String email);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) > 0 FROM Staff s WHERE s.email = :email AND s.id <> :id AND (s.isDeleted = false OR s.isDeleted IS NULL)")
    boolean existsByEmailAndIdNotAndActive(@org.springframework.data.repository.query.Param("email") String email,
            @org.springframework.data.repository.query.Param("id") UUID id);
}
