package com.medtrust.clinical.interfaces.rest;

import com.medtrust.clinical.application.dto.PatientResponse;
import com.medtrust.clinical.application.dto.RegisterPatientRequest;
import com.medtrust.clinical.application.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterPatientRequest request) {
        PatientResponse response = patientService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "data", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id) {
        PatientResponse response = patientService.findById(id);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }
}
