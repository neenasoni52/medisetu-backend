package com.medisetu.medisetu_backend.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.medisetu.medisetu_backend.entity.IntakeSession;

public interface IntakeSessionRepository extends JpaRepository<IntakeSession, Long> {
}