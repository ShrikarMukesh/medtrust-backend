package com.medtrust.integration.interfaces.rest;

import com.medtrust.integration.application.dto.*;
import com.medtrust.integration.application.service.IntegrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {

    private final IntegrationService integrationService;
    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    /** Receive FHIR resource from external system */
    @PostMapping("/fhir/inbound")
    public ResponseEntity<Map<String, Object>> inbound(@Valid @RequestBody InboundFhirRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "data", integrationService.processInbound(request)));
    }

    /** Export internal resource as FHIR to external system */
    @PostMapping("/fhir/outbound")
    public ResponseEntity<Map<String, Object>> outbound(@Valid @RequestBody OutboundFhirRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "data", integrationService.processOutbound(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("success", true, "data", integrationService.findById(id)));
    }

    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<Map<String, Object>> byCorrelation(@PathVariable String correlationId) {
        return ResponseEntity.ok(Map.of("success", true, "data",
                integrationService.findByCorrelationId(correlationId)));
    }

    @GetMapping("/direction/{direction}")
    public ResponseEntity<Map<String, Object>> byDirection(@PathVariable String direction) {
        return ResponseEntity.ok(Map.of("success", true, "data",
                integrationService.findByDirection(direction)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> byStatus(@PathVariable String status) {
        return ResponseEntity.ok(Map.of("success", true, "data",
                integrationService.findByStatus(status)));
    }

    @GetMapping("/system/{systemId}")
    public ResponseEntity<Map<String, Object>> bySystem(@PathVariable String systemId) {
        return ResponseEntity.ok(Map.of("success", true, "data",
                integrationService.findByExternalSystem(systemId)));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listAll() {
        return ResponseEntity.ok(Map.of("success", true, "data", integrationService.findAll()));
    }
}
