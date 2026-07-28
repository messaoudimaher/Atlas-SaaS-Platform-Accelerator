# Architectural Decision Record: ADR-001 - Microservices Framework Selection

## Status
Accepted

## Date
2026-07-28

## Context
When building a SaaS accelerator designed for startups and enterprises, selecting the right technology stack is critical. We need a language and framework that offers:
1.  **Strong Typing and Maintainability:** Essential for large, scaling codebases with multiple developers.
2.  **Rich Ecosystem:** Native support for security standards (OAuth2, OIDC), database routing, and integration libraries.
3.  **Modern Performance:** Support for modern JVM paradigms (like Virtual Threads) to minimize memory footprint and execution latency.
4.  **Developer Pool Availability:** High availability of experienced senior engineers.

### Alternatives Considered
*   **Node.js / NestJS (TypeScript):** Quick to bootstrap, but lacks native support for complex multi-tenant database routing out of the box and has weaker performance under heavy CPU-bound workloads.
*   **Go (Golang):** Highly performant and low memory footprint, but the ecosystem lacks mature, ready-to-use frameworks with security, OAuth2, and dependency injection comparable to Spring Boot.
*   **Java 21 / Spring Boot 3:** Strong typing, widespread enterprise adoption, mature ecosystem, virtual threads support, and excellent integration with Keycloak and Spring Cloud Gateway.

---

## Decision
We will build the Atlas SaaS Platform Accelerator using **Java 21** and **Spring Boot 3**. 

*   **Java 21:** Selected to leverage modern features like virtual threads (Project Loom) for high concurrency, structured concurrency, and records for immutable data transfers.
*   **Spring Boot 3:** Provides mature integrations for microservice patterns, robust security support, database transaction abstractions, and simplified application assembly.
*   **Spring Cloud Gateway:** Leverages reactive programming (Project Reactor and Netty) to serve as a low-latency, scalable entry point for routing and rate-limiting incoming requests.

---

## Consequences

### Positive
*   **Productivity:** Highly integrated ecosystem (Spring Security, Spring Data JPA, Spring Actuator) speeds up initial feature development.
*   **Concurrency Scaling:** Virtual Threads permit blocking I/O (database operations) to scale without depleting system thread pools, reducing memory consumption per request.
*   **Future Proof:** Wide adoption guarantees long-term platform viability.

### Negative
*   **Startup Overhead:** JVM applications generally have a longer startup time and higher initial memory footprint compared to Go or Node.js binary executables.
*   **Learning Curve:** Spring Boot's internal dependency injection patterns and configurations require senior-level knowledge to configure correctly.
