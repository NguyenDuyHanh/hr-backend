package com.tlu.hrm.repository;

import com.tlu.hrm.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    boolean existsByRoleId(UUID roleId);
}
