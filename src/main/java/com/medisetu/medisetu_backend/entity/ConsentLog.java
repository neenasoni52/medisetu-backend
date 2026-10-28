package com.medisetu.medisetu_backend.entity;



import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "consent_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

 // Links this consent record to the specific kiosk intake session
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private IntakeSession session;

 // True if patient gave consent ("I Agree"), False if declined ("No")
    @Column(nullable = false)
    private Boolean agreed;

 // DPDP purpose limitation: specifies what the data will be used for (e.g., "HISTORY_AND_DOCUMENT_PROCESSING"
    @Builder.Default
    @Column(nullable = false, length = 100)
    private String scope = "HISTORY_AND_DOCUMENT_PROCESSING";

 // Language in which consent text was shown to the patient ("hi", "mr", "en"
    @Column(name = "language_shown", nullable = false, length = 10)
    private String languageShown;

 // Version of the privacy/consent notice agreed to (e.g., "v1")
    @Builder.Default
    @Column(name = "text_version", nullable = false, length = 20)
    private String textVersion = "v1";
    
 // Immutable timestamp recording the exact moment consent was captured
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;
}