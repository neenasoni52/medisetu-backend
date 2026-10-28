package com.medisetu.medisetu_backend.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "intake_session")
public class IntakeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Language code selected on kiosk: "hi", "mr", "en"
    @Column(nullable = false, length = 10)
    private String language;

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SessionStatus status;

 // Automatic timestamp when the session record is created
    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;
    
    
    
    
    
    //====================
    
    @Builder.Default
    @Column(name = "active_module")
    private String activeModule = "CHIEF_COMPLAINT";

    @Builder.Default
    @Column(name = "red_flag_raised")
    private Boolean redFlagRaised = false;

    @Column(name = "red_flag_severity")
    private String redFlagSeverity; // "NONE", "YELLOW", "CRITICAL"

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    
    //Stores the AI-synthesized narrative summary of the patient's History of Present Illness (HPI) generated from their interview responses.
    @Column(name = "hpi_text", columnDefinition = "TEXT")
    private String hpiText;

    @Column(name = "doctor_notes", columnDefinition = "TEXT")
    private String doctorNotes;
    
    
    
}