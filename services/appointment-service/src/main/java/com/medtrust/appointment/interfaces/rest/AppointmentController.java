package com.medtrust.appointment.interfaces.rest;

import com.medtrust.appointment.application.dto.CreateAppointmentRequest;
import com.medtrust.appointment.application.dto.AppointmentResponse;
import com.medtrust.appointment.application.dto.RescheduleAppointmentRequest;
import com.medtrust.appointment.application.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentResponse response = appointmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "data", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String id) {
        AppointmentResponse response = appointmentService.getById(id);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable String id,
            @RequestParam(required = false) String reason) {
        AppointmentResponse response = appointmentService.cancel(id, reason);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<Map<String, Object>> reschedule(
            @PathVariable String id,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        AppointmentResponse response = appointmentService.reschedule(id, request);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirm(@PathVariable String id) {
        AppointmentResponse response = appointmentService.confirm(id);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> complete(@PathVariable String id) {
        AppointmentResponse response = appointmentService.complete(id);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }
}
