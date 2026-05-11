package com.tlu.hrm.repository;

import com.tlu.hrm.model.UserExt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserExtRepository extends JpaRepository<UserExt, UUID> {
}
