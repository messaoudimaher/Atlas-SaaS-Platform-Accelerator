# Product Discovery: Risk Analysis and Mitigation Strategies

This document identifies potential risks associated with building a multi-tenant cloud-native SaaS backend and defines mitigations for each.

---

## 1. Architectural and Technical Risks

### Risk 1.1: Data Leakage Between Tenants (Cross-Tenant Access)
*   **Description:** A bug in queries or dynamic routing configuration allows Tenant A to read or modify data belonging to Tenant B.
*   **Probability:** Medium
*   **Impact:** Critical
*   **Mitigation:** 
    1.  Implement a strict **database schema-per-tenant** or **database-per-tenant** model rather than a shared table with simple `where tenant_id = ?` clauses.
    2.  Use Spring Data dynamic datasource routing linked to the `ThreadLocal` context of the current request.
    3.  Enforce tenant validation at the Spring Cloud Gateway and downstream filters by verifying matching tenant IDs in the JWT token against requested resource scopes.

### Risk 1.2: Distributed Transaction Inconsistency (Event Delivery Failures)
*   **Description:** A user registers, but due to a network partition, the message broker is down, and the organization is not provisioned, leaving the system in an inconsistent state.
*   **Probability:** High
*   **Impact:** High
*   **Mitigation:**
    1.  Implement the **Transactional Outbox Pattern**. Write the event record to the database in the same transaction as the business model change.
    2.  Use a background publisher (e.g., Spring Scheduler or Debezium) to read the Outbox table and publish events to RabbitMQ with retry loops.
    3.  Enforce **Idempotent Consumers** downstream using event ID tracking tables to prevent duplicate processing of retried events.

### Risk 1.3: Gateway Performance Bottleneck
*   **Description:** Spring Cloud Gateway becomes a bottleneck as it processes high volumes of incoming traffic, runs authentication checks against Keycloak, and checks rate limits in Redis.
*   **Probability:** Medium
*   **Impact:** High
*   **Mitigation:**
    1.  Use stateless, reactive Spring Cloud Gateway (built on Netty) to handle non-blocking concurrent request processing.
    2.  Cache JWT validation results locally (with short TTLs) or use offline verification (validate JWT signature using Keycloak's JWKS endpoint and cache the public key).
    3.  Set up autoscaling (HPA) targets based on gateway CPU utilization and network throughput.

---

## 2. Security and Compliance Risks

### Risk 2.1: Keycloak Single Point of Failure (Auth Outage)
*   **Description:** If the Keycloak container or server fails, the entire application becomes inaccessible as no user can log in or validate tokens.
*   **Probability:** Medium
*   **Impact:** Critical
*   **Mitigation:**
    1.  Deploy Keycloak in high-availability mode (HA cluster) in production using replicated database configurations and load balancers.
    2.  Use external token caching at the client gateway layer where applicable.
    3.  Ensure robust liveness/readiness probes in the Keycloak Kubernetes manifest to recycle unhealthy pods instantly.

### Risk 2.2: Hardcoded Secrets in Source Code
*   **Description:** Developers inadvertently commit Keycloak client secrets, database passwords, or Stripe API keys to the public git repository.
*   **Probability:** High
*   **Impact:** Critical
*   **Mitigation:**
    1.  Set up **Gitleaks** as a pre-commit hook and in the CI/CD pipeline to block commits with credentials.
    2.  Enforce the Twelve-Factor app philosophy: all configuration parameters are injected via environment variables.
    3.  In production, map Kubernetes Secret objects directly into microservice pods at startup.
