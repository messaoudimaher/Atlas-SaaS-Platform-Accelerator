# Architectural Decision Record: ADR-004 - Transactional Outbox Pattern

## Status
Accepted

## Date
2026-07-28

## Context
When adopting an Event-Driven Architecture, microservices must notify other services of state changes (e.g., notifying the Billing Service that an Organization has registered). A major risk is the failure of distributed transactions:
1.  **Database Write succeeds, Broker Publish fails:** The database transaction commits, but the event is lost. Downstream services are out of sync.
2.  **Broker Publish succeeds, Database Write fails:** The event is published, but the database transaction rolls back. Downstream services act on incorrect data.

We need a reliable event publishing mechanism that guarantees **At-Least-Once event delivery** without the complexity of Two-Phase Commit (2PC) or XA transactions.

### Alternatives Considered
*   **Direct Publishing in Controller/Service:** Invoking `rabbitTemplate.convertAndSend()` directly inside the Spring business transaction. This is simple but prone to event loss or phantom events if the database transaction fails to commit.
*   **Transactional Outbox Pattern (Database-backed):** Write the event payload to an `outbox_events` database table inside the same transaction as the business entity update. An asynchronous daemon reads and publishes these events to RabbitMQ.
*   **Debezium / CDC (Change Data Capture):** Stream event updates from PostgreSQL write-ahead logs (WAL) to RabbitMQ/Kafka. Highly reliable but adds significant operational complexity (requires Kafka Connect, Debezium configurations, and more infrastructure).

---

## Decision
We choose the **Transactional Outbox Pattern** with a database table backer and a Spring-scheduled publisher thread as the default event-propagation pattern.

*   **Transactional Guarantee:** Both the business entity change and the outbox event write succeed or fail together, ensuring consistency.
*   **Spring Scheduler Publisher:** A light-weight scheduler polls the `outbox_events` table periodically, publishes pending events, and updates their status to `PROCESSED` upon broker acknowledgement.

---

## Consequences

### Positive
*   **Delivery Guarantees:** Ensures **At-Least-Once** delivery of events. If RabbitMQ is temporarily down, the events remain in the database outbox table until connection is restored.
*   **Performance Isolation:** Publishing to the broker happens asynchronously, preventing slow broker network response times from blocking customer-facing HTTP request threads.
*   **Simplicity:** Does not require heavy external tools like Kafka or Debezium during initial product phases.

### Negative
*   **Database Overhead:** Every state change write incurs an extra insert write into the `outbox_events` table. Outbox tables must be purged periodically of processed records.
*   **Latency:** Introduce a tiny event delivery delay (polling frequency dependent, e.g., 500ms).
*   **Idempotency Requirement:** As it guarantees at-least-once delivery, downstream consumers must implement idempotency checks to avoid duplicate event executions.
