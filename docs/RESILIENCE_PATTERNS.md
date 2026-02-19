# MedTrust — Microservices Resilience Patterns

Architecture overview of how resilience and integration patterns apply to the MedTrust Healthcare Platform.

## Architecture Flow

```mermaid
graph TB
    subgraph "Client Layer"
        CLIENT["Web / Mobile Client"]
    end

    subgraph "Gateway Layer"
        GW["API Gateway<br/><i>Routing · Auth · Rate Limiting · SSL</i>"]
    end

    subgraph "Service Discovery"
        SD["Service Registry<br/><i>K8s DNS / Eureka</i>"]
    end

    subgraph "Clinical Service"
        direction TB
        CS_API["REST Controllers"]
        CS_CB["Circuit Breaker<br/><i>Resilience4j</i>"]
        CS_RETRY["Retry<br/><i>Exp. Backoff + Jitter</i>"]
        CS_BH["Bulkhead<br/><i>Semaphore Isolation</i>"]
        CS_SVC["Application Services"]
        CS_DOMAIN["Domain Layer<br/><i>Patient · Encounter · ClinicalNote</i>"]
        CS_DB[("PostgreSQL<br/><i>clinical_service</i>")]

        CS_API --> CS_CB --> CS_RETRY --> CS_BH --> CS_SVC
        CS_SVC --> CS_DOMAIN
        CS_DOMAIN --> CS_DB
    end

    subgraph "Downstream Services"
        NS["Notification Service"]
        BS["Billing Service"]
        LS["Lab Service"]
    end

    subgraph "Event Backbone"
        KAFKA["Apache Kafka<br/><i>Event-Driven Choreography</i>"]
    end

    subgraph "Saga Orchestration"
        SAGA["Saga Coordinator<br/><i>Compensating Transactions</i>"]
    end

    CLIENT --> GW
    GW --> SD
    SD --> CS_API
    CS_SVC -->|"HTTP + CB + Retry + Bulkhead"| NS
    CS_SVC -->|"HTTP + CB + Retry + Bulkhead"| BS
    CS_SVC -->|"HTTP + CB + Retry + Bulkhead"| LS
    CS_SVC -->|"CB + Retry"| KAFKA
    KAFKA --> NS
    KAFKA --> BS
    SAGA --> CS_SVC
    SAGA --> NS
    SAGA --> BS
```

## Pattern Mapping

| # | Pattern | MedTrust Application |
|---|---------|---------------------|
| 1 | **Circuit Breaker** | `ExternalServiceClient` + `ClinicalKafkaProducer` — opens after 50% failure rate |
| 2 | **Retry** | Exponential backoff (×2) with randomized jitter on all external calls |
| 3 | **Bulkhead** | Semaphore isolation per downstream service (10 concurrent for notifications, 5 for billing) |
| 4 | **API Gateway** | Single entry point for routing, auth, rate limiting |
| 5 | **Service Discovery** | K8s DNS-based service resolution |
| 6 | **Database per Service** | `clinical_service` PostgreSQL — schema autonomy via Flyway |
| 7 | **Saga** | Distributed transactions for patient admission → billing → notification flows |
| 8 | **Event Choreography** | Kafka domain events (`PatientAdmitted`, `EncounterCreated`, `PatientDischarged`) |
| 9 | **Orchestrator** | Saga coordinator for complex multi-step workflows (future: Temporal) |
| 10 | **Strangler** | API Gateway routes new functionality to microservices, legacy modules decommissioned |

## Resilience4j Configuration Reference

### Circuit Breaker States

```mermaid
stateDiagram-v2
    [*] --> CLOSED
    CLOSED --> OPEN: Failure rate ≥ threshold
    OPEN --> HALF_OPEN: Wait duration elapsed
    HALF_OPEN --> CLOSED: Test calls succeed
    HALF_OPEN --> OPEN: Test calls fail
```

### Configuration Per Instance

| Parameter | Default | notificationService | kafkaProducer |
|-----------|---------|-------------------|---------------|
| Sliding window | 10 | 10 | 5 |
| Failure rate | 50% | 50% | 60% |
| Wait in open | 30s | 30s | 15s |
| Retry attempts | 3 | 3 | 3 |
| Retry backoff | 1s × 2^n | 1s × 2^n | 500ms × 2^n |
| Max concurrent | 10 | 10 | — |

### Retry Formula

```
delay = waitDuration × exponentialBackoffMultiplier^attempt × (1 ± randomizedWaitFactor)
```

Example (notificationService, attempt 2):
```
delay = 1000ms × 2^2 × (1 ± 0.5) = 4000ms ± 2000ms = [2000ms, 6000ms]
```

## Fallback Strategy

```mermaid
flowchart LR
    A[Call External Service] -->|Success| B[Return Response]
    A -->|Failure| C{Retry?}
    C -->|Under max attempts| A
    C -->|Exhausted| D{Circuit State?}
    D -->|CLOSED| E[Record Failure]
    D -->|OPEN| F[Fallback Method]
    F --> G[Log + Queue for Reprocessing]
```

## Anti-Patterns Avoided

| Anti-Pattern | How We Avoid It |
|---|---|
| Retry inside open circuit | Resilience4j executes retry BEFORE circuit breaker evaluates |
| Retry storms | Exponential backoff + jitter + max 3 attempts |
| Thread exhaustion | Bulkhead limits per downstream service |
| Cascading failures | Circuit breaker fast-fails + fallback methods |
| Shared database | PostgreSQL per service with Flyway migrations |
| Hidden coupling | Event-driven choreography via Kafka |
