package com.medtrust.notification.interfaces.rest;

import com.medtrust.notification.application.dto.*;
import com.medtrust.notification.application.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** Manual send endpoint */
    @PostMapping
    public ResponseEntity<Map<String, Object>> send(
            @Valid @RequestBody SendNotificationRequest request) {
        NotificationResponse response = notificationService.send(request, "manual");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "data", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("success", true, "data", notificationService.findById(id)));
    }

    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<Map<String, Object>> byRecipient(@PathVariable String recipientId) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", notificationService.findByRecipientId(recipientId)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> byStatus(@PathVariable String status) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", notificationService.findByStatus(status)));
    }

    @GetMapping("/channel/{channel}")
    public ResponseEntity<Map<String, Object>> byChannel(@PathVariable String channel) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", notificationService.findByChannel(channel)));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listAll() {
        return ResponseEntity.ok(Map.of("success", true,
                "data", notificationService.findAll()));
    }
}
