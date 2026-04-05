package com.medtrust.audit.interfaces.rest;

import com.medtrust.audit.application.dto.AuditEntryResponse;
import com.medtrust.audit.application.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Read-only audit trail API.
 * No POST/PUT/DELETE — audit entries are only created by the Kafka consumer.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("success", true, "data", auditService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listAll() {
        List<AuditEntryResponse> entries = auditService.findAll();
        return ResponseEntity.ok(Map.of("success", true, "data", entries, "count", entries.size()));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> byCategory(@PathVariable String category) {
        List<AuditEntryResponse> entries = auditService.findByCategory(category);
        return ResponseEntity.ok(Map.of("success", true, "data", entries, "count", entries.size()));
    }

    @GetMapping("/service/{sourceService}")
    public ResponseEntity<Map<String, Object>> bySourceService(@PathVariable String sourceService) {
        List<AuditEntryResponse> entries = auditService.findBySourceService(sourceService);
        return ResponseEntity.ok(Map.of("success", true, "data", entries, "count", entries.size()));
    }

    @GetMapping("/actor/{actorId}")
    public ResponseEntity<Map<String, Object>> byActor(@PathVariable String actorId) {
        List<AuditEntryResponse> entries = auditService.findByActorId(actorId);
        return ResponseEntity.ok(Map.of("success", true, "data", entries, "count", entries.size()));
    }

    @GetMapping("/target/{targetId}")
    public ResponseEntity<Map<String, Object>> byTarget(@PathVariable String targetId) {
        List<AuditEntryResponse> entries = auditService.findByTargetId(targetId);
        return ResponseEntity.ok(Map.of("success", true, "data", entries, "count", entries.size()));
    }

    @GetMapping("/event-type/{eventType}")
    public ResponseEntity<Map<String, Object>> byEventType(@PathVariable String eventType) {
        List<AuditEntryResponse> entries = auditService.findByEventType(eventType);
        return ResponseEntity.ok(Map.of("success", true, "data", entries, "count", entries.size()));
    }

    @GetMapping("/range")
    public ResponseEntity<Map<String, Object>> byDateRange(
            @RequestParam String from, @RequestParam String to) {
        List<AuditEntryResponse> entries = auditService.findByDateRange(from, to);
        return ResponseEntity.ok(Map.of("success", true, "data", entries, "count", entries.size()));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> count() {
        return ResponseEntity.ok(Map.of("success", true, "count", auditService.count()));
    }
}
