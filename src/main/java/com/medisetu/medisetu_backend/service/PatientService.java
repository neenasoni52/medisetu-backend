package com.medisetu.medisetu_backend.service;

import lombok.RequiredArgsConstructor;

import java.text.BreakIterator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medisetu.medisetu_backend.dto.request.CreatePatientRequest;
import com.medisetu.medisetu_backend.dto.request.PatientLookupRequest;
import com.medisetu.medisetu_backend.dto.response.PatientLookupResponse;
import com.medisetu.medisetu_backend.dto.response.PatientResponse;
import com.medisetu.medisetu_backend.entity.Patient;
import com.medisetu.medisetu_backend.exception.DuplicateResourceException;
import com.medisetu.medisetu_backend.exception.ResourceNotFoundException;
import com.medisetu.medisetu_backend.repository.PatientRepository;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    // Kiosk lookup: masks the surname for DPDP privacy on public screen
    public PatientLookupResponse lookupPatient(PatientLookupRequest request) {
        Patient patient = patientRepository.findByAnyIdentifier(request.getIdentifier().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with identifier: " + request.getIdentifier()));

        String maskedName = maskFullName(patient.getFullName());
        return PatientLookupResponse.builder()
                .patientId(patient.getId())
                .firstName(maskedName)
                .age(patient.getAge())
                .gender(patient.getGender())
                .build();
    }

    // Handles Registration: supports both permanent tokens (Desk) and temporary tokens (Kiosk)
    @Transactional
    public PatientResponse registerPatient(CreatePatientRequest request) {
        String token = (request.getTokenNumber() != null && !request.getTokenNumber().isBlank()) 
                ? request.getTokenNumber().trim() : null;
        
        String abha = (request.getAbhaNumber() != null && !request.getAbhaNumber().isBlank()) 
                ? request.getAbhaNumber().trim() : null;
        
        String phone = (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) 
                ? request.getPhoneNumber().trim() : null;

        if (token != null) {
            if (patientRepository.existsByTokenNumber(token)) {
                throw new DuplicateResourceException("Token number already registered: " + token);
            }
        }
        if (abha != null && patientRepository.existsByAbhaNumber(abha)) {
            throw new DuplicateResourceException("A record with this ABHA number already exists: " + abha);
        }
        if (phone != null && patientRepository.existsByPhoneNumber(phone)) {
            throw new DuplicateResourceException("A record with this phone number already exists: " + phone);
        }

        boolean isTemp = (token == null || token.isBlank());

        Patient patient = Patient.builder()
                .tokenNumber(token)
                .abhaNumber(abha)
                .phoneNumber(phone)
                .fullName(request.getFullName().trim())
                .age(request.getAge())
                .gender(request.getGender())
                .isTemporary(isTemp)
                .build();

        Patient saved = patientRepository.save(patient);

        // If walk-in with no token provided, auto-assign sequential temp token based on ID
        if (isTemp) {
            saved.setTokenNumber(String.format("temp%04d", saved.getId()));
            saved = patientRepository.save(saved);
        }

        return mapToResponse(saved);
    }

    @Transactional
    public PatientResponse registerPatientAtKiosk(CreatePatientRequest request) {
        return registerPatient(request);
    }

    // Returns all patients for Registration Desk, or only permanent token patients for Doctor
    public List<PatientResponse> getAllPatients(boolean onlyPermanent) {
        List<Patient> list = onlyPermanent 
                ? patientRepository.findByIsTemporaryFalseOrderByIdDesc()
                : patientRepository.findAllByOrderByIdDesc();
        
        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PatientResponse mapToResponse(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId())
                .tokenNumber(patient.getTokenNumber())
                .abhaNumber(patient.getAbhaNumber())
                .phoneNumber(patient.getPhoneNumber())
                .fullName(patient.getFullName())
                .age(patient.getAge())
                .gender(patient.getGender())
                .isTemporary(patient.isTemporary())
                .createdAt(patient.getCreatedAt())
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