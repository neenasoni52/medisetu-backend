package com.medisetu.medisetu_backend.controller;

import com.medisetu.medisetu_backend.dto.request.CreatePatientRequest;
import com.medisetu.medisetu_backend.dto.request.PatientLookupRequest;
import com.medisetu.medisetu_backend.dto.response.PatientLookupResponse;
import com.medisetu.medisetu_backend.dto.response.PatientResponse;
import com.medisetu.medisetu_backend.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("medisetu/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // Registers patient (supports both Desk permanent tokens and Kiosk temporary tokens)
    @PostMapping
    public ResponseEntity<PatientResponse> registerPatient(@Valid @RequestBody CreatePatientRequest request) {
        PatientResponse response = patientService.registerPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Fetches patient list: all for registration desk, or only permanent for doctor
    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients(
            @RequestParam(required = false, defaultValue = "false") boolean onlyPermanent) {
        List<PatientResponse> response = patientService.getAllPatients(onlyPermanent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/lookup")
    public ResponseEntity<PatientLookupResponse> lookupPatient(@Valid @RequestBody PatientLookupRequest request) {
        PatientLookupResponse response = patientService.lookupPatient(request);
        return ResponseEntity.ok(response);
    }
}