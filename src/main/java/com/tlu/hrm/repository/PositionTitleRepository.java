package com.tlu.hrm.repository;

import com.tlu.hrm.model.PositionTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PositionTitleRepository extends JpaRepository<PositionTitle, UUID> {
}
