# Architecture: Containers (C4 Level 2)

This document shows the container architecture (C4 Level 2) for the **Atlas SaaS Platform Accelerator**. It illustrates how logical microservices, caches, and databases are organized and communicate with each other.

---

## 1. Container Diagram

```mermaid
graph TD
    Client["Web Browser / API Client<br>(Single Page Application / Postman)"]

    subgraph PlatformBoundary ["Atlas Platform Boundary"]
        %% Gateway
        Gateway["Spring Cloud Gateway<br>(Java/Netty Gateway)<br>[Token validation & Rate limiting]"]

        %% Services
        IdentityService["Identity & Auth Service<br>(Spring Boot Service)"]
        OrgService["Organization Service<br>(Spring Boot Service)<br>[Orgs, Workspaces, Teams]"]
        BillingService["Billing Service<br>(Spring Boot Service)<br>[Stripe & Plan Quotas]"]
        StorageService["File Storage Service<br>(Spring Boot Service)<br>[MinIO S3 uploads]"]
        NotificationService["Notification Service<br>(Spring Boot Service)<br>[Email and Webhook dispatches]"]
        ApiKeyService["API Key Service<br>(Spring Boot Service)<br>[Programmatic access]"]

        %% Message Broker
        RabbitMQ["RabbitMQ Message Broker<br>(AMQP Event Bus)<br>[Transactional Outbox Events]"]

        %% Shared Caches
        Redis["Redis Cache Cluster<br>(Key-Value store)<br>[Rate Limit buckets & session cache]"]

        %% Storage Node
        MinIO["MinIO Object Storage<br>(S3 compatible storage)<br>[User assets / backups]"]

        %% Service Databases (No Shared Database)
        subgraph Databases ["Isolated Postgres Databases"]
            OrgDB[("Org PostgreSQL DB<br>(Schema per Tenant)")]
            BillingDB[("Billing PostgreSQL DB")]
            StorageDB[("Storage PostgreSQL DB")]
            NotifyDB[("Notification PostgreSQL DB")]
            ApiKeyDB[("API Key PostgreSQL DB")]
        end
    end

    %% External Systems
    Keycloak["Keycloak Server<br>(Identity Federated Store)"]
    Stripe["Stripe Gateway<br>(Payment Processor)"]

    %% Flow arrows
    Client -->|HTTPS requests| Gateway
    Gateway -->|Redis rate check| Redis
    Gateway -->|Routes with headers| IdentityService
    Gateway -->|Routes| OrgService
    Gateway -->|Routes| BillingService
    Gateway -->|Routes| StorageService
    Gateway -->|Routes| ApiKeyService

    %% Inter-service events
    OrgService -->|Outbox publish| RabbitMQ
    BillingService -->|Outbox publish| RabbitMQ
    StorageService -->|Outbox publish| RabbitMQ

    %% Event consumption
    RabbitMQ -->|Consume events| NotificationService

    %% Database bindings
    OrgService --> OrgDB
    BillingService --> BillingDB
    StorageService --> StorageDB
    NotificationService --> NotifyDB
    ApiKeyService --> ApiKeyDB

    %% External bindings
    IdentityService -->|Syncs accounts & tokens| Keycloak
    BillingService -->|Syncs cards & plan| Stripe
    StorageService -->|Uploads binary files| MinIO

    classDef container fill:#1168BD,stroke:#0B4E8F,color:#fff;
    classDef broker fill:#D85A2B,stroke:#993C18,color:#fff;
    classDef cache fill:#A41E11,stroke:#70120A,color:#fff;
    classDef db fill:#005E5D,stroke:#003E3E,color:#fff;
    classDef client fill:#08427B,stroke:#052E56,color:#fff;
    classDef ext fill:#999999,stroke:#666666,color:#fff;

    class Gateway,IdentityService,OrgService,BillingService,StorageService,NotificationService,ApiKeyService container;
    class RabbitMQ broker;
    class Redis cache;
    class OrgDB,BillingDB,StorageDB,NotifyDB,ApiKeyDB db;
    class MinIO db;
    class Client client;
    class Keycloak,Stripe ext;
```

---

## 2. Container Registry and Roles

### Spring Cloud Gateway (Port `8080`)
*   **Role:** Acts as the reverse proxy for all inbound REST APIs.
*   **Features:** Intercepts traffic, queries Redis to verify rate limits, uses public Keycloak certificates to validate JWT signatures offline, extracts tenant claims, and forwards metadata headers downstream.

### Identity & Auth Service (Port `8081`)
*   **Role:** Manages the integration bridge with Keycloak.
*   **Features:** Handles registration callback validation, profiles mapping, password reset requests redirection, and role synchronization.

### Organization Service (Port `8082`)
*   **Role:** Manages tenants, workspaces, and memberships.
*   **Features:** Handles organization creation, team structures, workspace division, and membership invites. Uses dynamic routing to isolate each organization's data into its own PostgreSQL schema (Schema-per-Tenant model).

### Billing Service (Port `8083`)
*   **Role:** Governs subscriptions, billing cycles, and feature locks.
*   **Features:** Implements a pluggable billing strategy via the `BillingProvider` interface. Enforces usage quotas in real-time.

### File Storage Service (Port `8084`)
*   **Role:** Secure file ingestion.
*   **Features:** Translates client uploads into private/public S3 bucket uploads in MinIO. Stores file size and tenant attribution metadata in PostgreSQL.

### Notification Service (Port `8085`)
*   **Role:** Orchestrates outbound notifications.
*   **Features:** Consumes events published to RabbitMQ (e.g., `UserRegistered`, `InvitationAccepted`) and dispatches emails via SMTP or triggers outbound webhooks.

### API Key Service (Port `8086`)
*   **Role:** Programmatic credentials validation.
*   **Features:** Generates and validates hash-based keys for external scripts or developer integrations.

---

## 3. Shared Caches and Datastores

*   **Redis:** Local high-performance cache. Used by the Gateway for IP/tenant rate-limiting counts, and by core services for tenant metadata and plan quotas lookup.
*   **Isolated PostgreSQL Instances:** Each microservice operates its own database. Services NEVER directly read or write to other services' tables, enforcing strict microservice coupling boundaries.
*   **RabbitMQ:** Enforces event-driven data flow across services (e.g. Org Service alerts Billing Service via event when an organization is deleted).
*   **MinIO Object Storage:** Serves as the developer-friendly S3 interface for storing document uploads, profile pictures, and backups.
