package com.tlu.hrm.repository;

import com.tlu.hrm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByStaffId(UUID staffId);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND u.staff IS NOT NULL AND u.staff.department.id = :deptId")
    List<User> findUsersByDepartmentId(@Param("deptId") UUID deptId);
}
