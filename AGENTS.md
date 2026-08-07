# Agent Guidelines and Code Standards

This file establishes the architectural guidelines, code standards, and conventions to be followed by all AI coding assistants (agents) contributing to the **Atlas SaaS Platform Accelerator**.

---

## 1. Core Architectural Guidelines

### Domain-Driven Design (DDD) & Hexagonal Architecture
*   **Domain Isolation:** The core domain logic (`/domain`) must be pure Java, containing no framework dependencies (no Spring annotations, no JPA/Hibernate annotations).
*   **Ports & Adapters:**
    *   **Driver Ports (Usecases):** Interfaces exposed by the domain for primary actors (REST controllers, event listeners).
    *   **Driven Ports (SPIs):** Interfaces exposed by the domain for secondary operations (repositories, event publishers).
    *   **Adapters:** Implementations of ports (e.g., JPA repositories, REST controllers). All framework-specific configurations live in the adapters layer.

### Multi-Tenancy Rules
*   **Dynamic Schema Context:** Intercept incoming request headers (`X-Tenant-Id`, `X-User-Id`) and bind them to the local `TenantContext`.
*   **Memory Context Cleanup:** You must ALWAYS register a request lifecycle hook (interceptor `afterCompletion` or filter `finally` block) to execute `TenantContext.clear()`. Failure to do so exposes the tenant to cross-request data leaks.
*   **Database Coupling:** Under no circumstances should multiple services share a database instance or schema. Services communicate exclusively via REST endpoints or RabbitMQ events.

---

## 2. Java and Spring Boot Coding Conventions

*   **Java Version:** Target **Java 21**. Leverage record classes for immutable Data Transfer Objects (DTOs), pattern matching, text blocks, and virtual thread models where I/O blocking occurs.
*   **Lombok Usage:**
    *   Use explicit `@Getter` and `@Setter` annotations.
    *   Avoid `@Data` on JPA/Hibernate entities, as it overrides `equals()` and `hashCode()` in a way that breaks lazy loading proxies and entity collections.
*   **Transactional Outbox Pattern:**
    *   Any business operation publishing events to RabbitMQ must insert the event payload into the service's `outbox_events` table in the same transaction as the business model change.
    *   Do not trigger direct publishing to the message broker inside user-facing REST threads.

---

## 3. Git Commit and Tracking Rules

*   **Conventional Commits:** Follow the conventional commit format for all changes (e.g., `feat(org): ...`, `fix(auth): ...`, `build(gradle): ...`).
*   **Markdown Constraints:** Respect the user's push constraints. All files under `docs/` and project status files (`PROJECT_STATUS.md`, `CHANGELOG.md`) are gitignored and must remain local to the workspace; do not force-stage them. Root documentation files `README.md` and `AGENTS.md` are exceptions and must be committed and pushed.
