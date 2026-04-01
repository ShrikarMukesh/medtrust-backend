package com.medtrust.patient.interfaces.rest;

import com.medtrust.patient.application.dto.*;
import com.medtrust.patient.application.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/mrn/{mrn}")
    public ResponseEntity<Map<String, Object>> getByMrn(@PathVariable String mrn) {
        PatientResponse response = patientService.findByMrn(mrn);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listAll(
            @RequestParam(required = false) String lastName) {
        List<PatientResponse> patients;
        if (lastName != null && !lastName.isBlank()) {
            patients = patientService.searchByLastName(lastName);
        } else {
            patients = patientService.findAll();
        }
        return ResponseEntity.ok(Map.of("success", true, "data", patients));
    }

    @PutMapping("/{id}/contact")
    public ResponseEntity<Map<String, Object>> updateContactInfo(
            @PathVariable String id,
            @Valid @RequestBody UpdateContactInfoRequest request) {
        PatientResponse response = patientService.updateContactInfo(id, request);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @PutMapping("/{id}/insurance")
    public ResponseEntity<Map<String, Object>> updateInsurance(
            @PathVariable String id,
            @Valid @RequestBody UpdateInsuranceRequest request) {
        PatientResponse response = patientService.updateInsurance(id, request);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deactivate(@PathVariable String id) {
        PatientResponse response = patientService.deactivate(id);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }
}
