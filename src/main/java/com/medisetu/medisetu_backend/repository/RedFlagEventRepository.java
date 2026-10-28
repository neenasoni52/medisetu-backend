package com.medisetu.medisetu_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medisetu.medisetu_backend.entity.RedFlagEvent;

import java.util.List;

public interface RedFlagEventRepository extends JpaRepository<RedFlagEvent, Long> {
    List<RedFlagEvent> findBySessionId(Long sessionId);
}