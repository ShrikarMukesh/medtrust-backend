# MedTrust Backend — AI Agent Guidelines

## Architecture Overview

MedTrust is a **Domain-Driven Design (DDD) microservices architecture** for healthcare SaaS, emphasizing compliance, event-driven reliability, and bounded contexts.

### Service Boundaries
- **appointment-service**: Manages appointments, schedules, rescheduling, cancellations
- **clinical-service**: Handles patients, encounters, clinical notes
- **auth-service**: Authentication and authorization
- **consent-service**: Consent management for PHI access
- **audit-service**: Immutable audit logging
- **notification-service**: Notifications via SMS/email
- **integration-service**: External system integrations
- **patient-service**: Patient data management

Each service owns its PostgreSQL database with Flyway migrations (e.g., `services/appointment-service/src/main/resources/db/migration/V1__init_appointments.sql`).

### Data Flow
- **Event-driven choreography** via Apache Kafka (KRaft mode, no Zookeeper)
- Domain events published after aggregate changes (e.g., `AppointmentScheduledEvent`)
- RabbitMQ for direct messaging (e.g., SMS notifications)
- Transactional outbox pattern for guaranteed event publishing

### Tech Stack
- **Java 21**, Spring Boot 3.4, Maven
- **PostgreSQL** per service, **MongoDB** for unstructured data
- **Kafka** for events, **RabbitMQ** for commands
- **Docker** + **Kubernetes** (Kind for local dev), **Helm** for deployments

## Key Patterns & Conventions

### Hexagonal Architecture Layers
- **Domain** (`domain/`): Aggregates, entities, events, repositories (interfaces)
- **Application** (`application/`): Services, DTOs, use cases
- **Infrastructure** (`infrastructure/`): Adapters (JPA, Kafka, RabbitMQ), configs
- **Interfaces** (`interfaces/`): REST controllers, exception handlers

### Domain Modeling
- Aggregates with factory methods (`Appointment.schedule()`), business logic methods (`cancel()`, `reschedule()`)
- Domain events collected in aggregates, pulled and published after save
- Records for immutable events/DTOs (e.g., `AppointmentScheduledEvent`)
- UUID primary keys, enum types (e.g., `AppointmentStatus`, `AppointmentType`)

### Persistence
- JPA entities with `@Entity`, Flyway migrations
- Repository pattern: Domain interface → Adapter → Spring Data JPA
- Adapter maps between domain models and JPA entities (e.g., `JpaAppointmentRepositoryAdapter`)

### Resilience & Reliability
- **Resilience4j**: Circuit breakers on external calls (Kafka, RabbitMQ), retries with exponential backoff + jitter, bulkheads per service
- Configured in `application.yml` (e.g., Kafka producer circuit: 5-call window, 60% failure threshold, 15s open state)
- Fallback methods for graceful degradation

### Messaging
- Kafka: Events with event type as key, JSON payload (e.g., `appointment-events` topic)
- RabbitMQ: Direct exchanges with routing keys (e.g., `notification.exchange`, `sms.routing.key`)

## Developer Workflows

### Local Development
- Run single service: `cd services/appointment-service && ./mvnw spring-boot:run`
- Full stack: `docker-compose up` (Postgres + RabbitMQ) + Kafka via K8s/Kind
- Build: `./mvnw clean package` (includes tests)
- Debug: Enable SQL logging in `application.yml` (`jpa.show-sql: true`)

### Testing
- Unit tests in `src/test/`, integration tests with `@SpringBootTest`
- TestContainers for DB/Kafka in integration tests
- Actuator health checks: `/actuator/health`

### Deployment
- **Local K8s**: `./infrastructure/k8s/deploy.sh` (creates Kind cluster, builds images, deploys infra + services)
- **Helm charts** per service (`services/*/helm/`): `helm install appointment-service services/appointment-service/helm/ --namespace medtrust`
- **Infrastructure**: Raw K8s YAML for Postgres, Kafka, RabbitMQ
- **CI/CD**: Event-driven validation via Kafka schema compatibility

### Common Commands
- Build image: `docker build -t medtrust/appointment-service:latest services/appointment-service/`
- Load to Kind: `kind load docker-image medtrust/appointment-service:latest --name medtrust-cluster`
- Restart deployment: `kubectl rollout restart deployment/appointment-service -n medtrust`
- View logs: `kubectl logs -l app=appointment-service -n medtrust`
- Port forward: `kubectl port-forward svc/appointment-service 8082:8082 -n medtrust`

## Integration Points

### External Dependencies
- **Kafka Schema Registry**: Avro schemas for event evolution (backward-compatible)
- **OIDC/JWT**: For auth (OAuth2 flows)
- **Field-level encryption**: For PHI data
- **RBAC + ABAC**: Authorization checks in services

### Cross-Service Communication
- REST APIs for synchronous calls (with circuit breakers)
- Events for asynchronous updates (e.g., appointment events trigger notifications)
- Shared nothing: No database joins across services

### Configuration
- Environment variables in `application.yml` (e.g., `KAFKA_BROKER`, `DB_HOST`)
- Secrets via K8s Secrets/ConfigMaps
- Feature flags via config properties

## Specific Conventions

- **Package structure**: `com.medtrust.{service}.{layer}` (e.g., `com.medtrust.appointment.domain.model`)
- **Naming**: Kebab-case for services/files (e.g., `appointment-service`), camelCase for classes
- **Error handling**: Custom exceptions, global `@RestControllerAdvice` for REST responses
- **Logging**: SLF4J with structured logs (e.g., event publishing confirmations)
- **Validation**: Bean Validation (`@Valid`) on DTOs, custom validators for business rules
- **Security**: JWT tokens, role-based access, consent checks before PHI access

## Examples

- **Aggregate with events**: `Appointment` class publishes `AppointmentScheduledEvent` on creation
- **Repository adapter**: `JpaAppointmentRepositoryAdapter.toDomain()` reconstructs aggregate from entity
- **Kafka producer**: `@CircuitBreaker` + `@Retry` on `publish()`, fallback logs for reprocessing
- **Controller**: REST endpoints return `{"success": true, "data": {...}}` wrapper
- **Migration**: Flyway SQL creates tables with UUID columns, indexes on foreign keys

Reference: `services/appointment-service/` exemplifies all patterns.</content>
<parameter name="filePath">/home/shrikar/2026-Dev/medtrust-backend/AGENTS.md
