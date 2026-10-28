package com.medisetu.medisetu_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "red_flag_event")
@Getter @Setter @NoArgsConstructor
public class RedFlagEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "rule_id", nullable = false)
    private String ruleId;

    @Column(name = "severity", nullable = false)
    private String severity; // "YELLOW", "CRITICAL"

    @Column(name = "trigger_details", length = 1000)
    private String triggerDetails;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}