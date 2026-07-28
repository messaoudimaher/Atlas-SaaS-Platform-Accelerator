# Product Discovery: Backlog and Milestones

This document captures the Product Backlog, initial Sprint Backlogs, and the Definition of Done (DoD) for the **Atlas SaaS Platform Accelerator**.

---

## 1. Product Backlog

Estimated in Story Points (SP) using the Fibonacci scale (1 = minimal effort, 13 = high risk/complexity):

| ID | Title | Epic | Estimate (SP) | Priority |
| :--- | :--- | :--- | :--- | :--- |
| **US-1.1** | Tenant Organization Provisioning | Identity & Tenant Management | 5 | Critical |
| **US-1.2** | Member Invitations (token-based) | Identity & Tenant Management | 3 | High |
| **US-2.1** | Secure OAuth2/OIDC/JWT Login | Auth & Access Controls | 8 | Critical |
| **US-2.2** | Programmatic API Key Management | Auth & Access Controls | 5 | Medium |
| **US-3.1** | Tier-based Plan Limits (Workspaces/Users) | Subscriptions & Billing | 8 | Critical |
| **US-3.2** | Billing Provider Integration (Stripe Skeleton) | Subscriptions & Billing | 5 | High |
| **US-4.1** | S3-Compatible File Upload (MinIO) | Shared Infrastructure Services | 5 | Medium |
| **US-5.1** | Transactional Outbox Pattern | Event-Driven Architecture | 8 | High |
| **US-5.2** | RabbitMQ Reliable Consumer & DLQ | Event-Driven Architecture | 5 | High |
| **US-6.1** | Distributed Tracing & Correlation IDs | Platform Observability | 5 | Medium |
| **US-6.2** | Prometheus metrics & Grafana Dashboard | Platform Observability | 5 | Medium |
| **US-7.1** | Kubernetes Helm Deployment Setup | Infrastructure as Code | 8 | High |

---

## 2. Sprint Backlogs

### Sprint 1: Product Discovery & Architecture Alignment (Current)
*   **Goal:** Establish all product definitions, target roadmaps, system container boundaries, API-first specifications, and workspace directory structure.
*   **Backlog Items:**
    *   Phase 1: Product Discovery (Vision, Requirements, Backlogs, Risk Assessment)
    *   Phase 2: System Architecture Design (C4 Models, ADRs, Schema isolation plan)
    *   **Estimate Total:** 8 SP

### Sprint 2: Repository Setup & Infrastructure Core
*   **Goal:** Initialize the project's build skeleton, configuration setup, and basic docker orchestration.
*   **Backlog Items:**
    *   Phase 3: Root Gradle build, conventions plugins, gateway core, and API models
    *   Phase 4: Docker compose configurations for local development stack (Postgres, RabbitMQ, Redis, Keycloak)
    *   **Estimate Total:** 13 SP

### Sprint 3: Identity & Basic Tenant Isolation
*   **Goal:** Configure auth token enforcement and tenant schemas.
*   **Backlog Items:**
    *   Phase 5: Keycloak realm settings, JWT claims logic, gateway route authorization
    *   Database schema-per-tenant dynamic routing integration
    *   **Estimate Total:** 13 SP

---

## 3. Detailed Milestones

### Milestone 1.1: Product Discovery & Project Tracking
*   **Objectives:** Establish project status tracking, alignment documentation, and detailed roadmap.
*   **Deliverables:** `PROJECT_STATUS.md`, `ROADMAP.md`, `CHANGELOG.md`, and `docs/product-discovery/`.
*   **Dependencies:** None.
*   **Estimated Complexity:** Low (1 SP).
*   **Definition of Done (DoD):**
    *   All discovery files written with comprehensive details (no placeholder text).
    *   Status, roadmap, and changelog files created in the workspace root.
    *   All documentation validates as correct markdown.

### Milestone 1.2: Architecture & C4 Blueprints
*   **Objectives:** Define the overall structural boundaries, database relations, and architectural rules.
*   **Deliverables:** C4 Model markdown/diagrams, ERDs, ADRs.
*   **Dependencies:** Milestone 1.1.
*   **Estimated Complexity:** Medium (3 SP).
*   **DoD:**
    *   System, Container, Component, and Deployment diagrams created.
    *   Architectural Decision Records (ADRs) written for framework patterns.
    *   Database-per-tenant isolation approach mapped.
