package com.tlu.hrm.repository;

import com.tlu.hrm.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<Staff, UUID> {
    Staff findFirstByOrderByStaffCodeDesc();

    @Query("SELECT s.staffCode FROM Staff s WHERE s.staffCode LIKE 'NV%_%'")
    List<String> findMaxValidStaffCode();

    List<Staff> findByIsDeletedFalse();

    @Query("SELECT s FROM Staff s WHERE s.isDeleted = false OR s.isDeleted IS NULL")
    List<Staff> findActiveStaffs();

    @Query("SELECT COUNT(s) > 0 FROM Staff s WHERE s.email = :email AND (s.isDeleted = false OR s.isDeleted IS NULL)")
    boolean existsByEmailAndActive(@Param("email") String email);

    @Query("SELECT COUNT(s) > 0 FROM Staff s WHERE s.email = :email AND s.id <> :id AND (s.isDeleted = false OR s.isDeleted IS NULL)")
    boolean existsByEmailAndIdNotAndActive(@Param("email") String email,
            @Param("id") UUID id);

    @Query("SELECT COUNT(s) > 0 FROM Staff s WHERE s.department.id = :deptId AND (s.isDeleted = false OR s.isDeleted IS NULL)")
    boolean existsActiveStaffByDepartmentId(@Param("deptId") UUID deptId);

    @Query("SELECT COUNT(s) > 0 FROM Staff s WHERE s.position.id = :posId AND (s.isDeleted = false OR s.isDeleted IS NULL)")
    boolean existsActiveStaffByPositionId(@Param("posId") UUID posId);
}
