# Appointment Service

This service manages patient appointments, handling scheduling, rescheduling, cancellations, and status updates. It is built using Spring Boot, PostgreSQL, and Kafka.

## API Reference

The service runs on port `8082`.

### 1. Create Appointment

```bash
curl -X POST http://localhost:8082/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "550e8400-e29b-41d4-a716-446655440000",
    "providerId": "c4996409-5a8e-4a67-b765-654e5658097b",
    "startTime": "2026-03-01T10:00:00Z",
    "endTime": "2026-03-01T10:30:00Z",
    "type": "CHECKUP",
    "reason": "Routine annual physical examination"
  }' | json_pp
```

### 2. Get Appointment by ID

Replace `{id}` with the UUID returned from the creation step.

```bash
curl -X GET http://localhost:8082/api/appointments/{id} | json_pp
```

### 3. Confirm Appointment

```bash
curl -X PUT http://localhost:8082/api/appointments/{id}/confirm | json_pp
```

### 4. Reschedule Appointment

```bash
curl -X PUT http://localhost:8082/api/appointments/{id}/reschedule \
  -H "Content-Type: application/json" \
  -d '{
    "newStartTime": "2026-03-02T14:00:00Z",
    "newEndTime": "2026-03-02T14:30:00Z"
  }' | json_pp
```

### 5. Complete Appointment

```bash
curl -X PUT http://localhost:8082/api/appointments/{id}/complete | json_pp
```

### 6. Cancel Appointment

```bash
curl -X PUT "http://localhost:8082/api/appointments/{id}/cancel?reason=PatientRequest" | json_pp
```

---

## Configuration

- **Port**: 8082
- **Database**: PostgreSQL (`appointment_service`)
- **Messaging**: Kafka (`appointment-events`)
