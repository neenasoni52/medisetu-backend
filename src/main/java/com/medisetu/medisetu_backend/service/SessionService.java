package com.medisetu.medisetu_backend.service;

import lombok.RequiredArgsConstructor;

import java.text.BreakIterator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medisetu.medisetu_backend.dto.request.StartSessionRequest;
import com.medisetu.medisetu_backend.dto.response.SessionResponse;
import com.medisetu.medisetu_backend.entity.ConsentLog;
import com.medisetu.medisetu_backend.entity.IntakeSession;
import com.medisetu.medisetu_backend.entity.Patient;
import com.medisetu.medisetu_backend.entity.SessionStatus;
import com.medisetu.medisetu_backend.exception.ResourceNotFoundException;
import com.medisetu.medisetu_backend.repository.ConsentLogRepository;
import com.medisetu.medisetu_backend.repository.IntakeSessionRepository;
import com.medisetu.medisetu_backend.repository.PatientRepository;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final IntakeSessionRepository sessionRepository;
    private final PatientRepository patientRepository;
    private final ConsentLogRepository consentLogRepository;

    @Transactional
    public SessionResponse startSession(StartSessionRequest request) {
        System.out.println("getting patient by id");
    	Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
         System.out.println("patient found!!!");
        IntakeSession session = IntakeSession.builder()
                .patient(patient)
                .language(request.getLanguage().trim())
                .status(SessionStatus.STARTED)
                .build();

        IntakeSession savedSession = sessionRepository.save(session);
        System.out.println("Intake session made and saved");
        ConsentLog consentLog = ConsentLog.builder()
                .session(savedSession)
                .agreed(true)
                .languageShown(request.getLanguage().trim())
                .build();

        consentLogRepository.save(consentLog);
        System.out.println("consent log made and saved");
        return mapToResponse(savedSession,patient);
       
    }

   

    public SessionResponse getSession(Long sessionId) {
        IntakeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        return mapToResponse(session,session.getPatient());
    }

    private SessionResponse mapToResponse(IntakeSession session,Patient patient) {
    	if (session == null || patient == null) {
            throw new IllegalArgumentException("Session and Patient cannot be null during mapping");
        }
    	
        return SessionResponse.builder()
                .sessionId(session.getId())
                .patientId(session.getPatient().getId())
                .tokenNumber(patient.getTokenNumber())
                .isTemporary(patient.isTemporary())
                .maskedName(maskFullName(patient.getFullName()))
                .language(session.getLanguage())
                .sessionStatus(session.getStatus())
                .createdAt(session.getStartedAt())
                .build();
    }
    
    
    
    private String maskFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0];
        }

        String firstName = parts[0];
        String lastName = parts[parts.length - 1];

        BreakIterator iterator = BreakIterator.getCharacterInstance();
        iterator.setText(lastName);
        int firstCharEnd = iterator.next();

        String surnamePrefix = lastName.substring(0, firstCharEnd);
        return firstName + " " + surnamePrefix + "***";
    }
}