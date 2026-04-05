package com.medtrust.consent.interfaces.rest;

import com.medtrust.consent.application.dto.*;
import com.medtrust.consent.application.service.ConsentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consents")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> grantConsent(
            @Valid @RequestBody GrantConsentRequest request) {
        ConsentResponse response = consentService.grantConsent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "data", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id) {
        ConsentResponse response = consentService.findById(id);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Map<String, Object>> getByPatientId(@PathVariable String patientId) {
        List<ConsentResponse> consents = consentService.findByPatientId(patientId);
        return ResponseEntity.ok(Map.of("success", true, "data", consents));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getByUserId(@PathVariable String userId) {
        List<ConsentResponse> consents = consentService.findByGrantedToUserId(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", consents));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listAll() {
        List<ConsentResponse> consents = consentService.findAll();
        return ResponseEntity.ok(Map.of("success", true, "data", consents));
    }

    /**
     * Core verification endpoint.
     * Other services call: GET /api/consents/verify?patientId=...&userId=...&scope=...
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyConsent(
            @RequestParam String patientId,
            @RequestParam String userId,
            @RequestParam String scope) {
        ConsentVerificationResponse response = consentService.verifyConsent(patientId, userId, scope);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> revokeConsent(@PathVariable String id) {
        ConsentResponse response = consentService.revokeConsent(id);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }
}
