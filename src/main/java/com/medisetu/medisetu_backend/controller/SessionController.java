package com.medisetu.medisetu_backend.controller;

import com.medisetu.medisetu_backend.dto.request.StartSessionRequest;
import com.medisetu.medisetu_backend.dto.response.SessionResponse;
import com.medisetu.medisetu_backend.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("medisetu/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/start")
    public ResponseEntity<SessionResponse> startSession(@Valid @RequestBody StartSessionRequest request) {
        SessionResponse response = sessionService.startSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable Long sessionId) {
        SessionResponse response = sessionService.getSession(sessionId);
        return ResponseEntity.ok(response);
    }
}