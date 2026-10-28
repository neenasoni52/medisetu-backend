package com.medisetu.medisetu_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medisetu.medisetu_backend.entity.ConsentLog;

public interface ConsentLogRepository extends JpaRepository<ConsentLog, Long> {
}