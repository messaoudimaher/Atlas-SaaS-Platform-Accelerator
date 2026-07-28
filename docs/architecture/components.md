# Architecture: Components (C4 Level 3)

This document drills down into C4 Level 3 Component diagrams for key blocks: the **Spring Cloud Gateway** and the **Organization Service**.

---

## 1. Spring Cloud Gateway Component Diagram

The Gateway acts as the secure ingress controller for all platform services, coordinating rate limits, security, and routing.

```mermaid
graph TD
    Client["Client / User Agent"] -->|HTTP Request| Netty["Reactive Netty Web Server"]

    subgraph GatewayComponents ["Spring Cloud Gateway Components"]
        Netty --> RouteLocator["Route Locator<br>(Dynamic Path Mapping)"]

        subgraph FilterChain ["Gateway Global Filter Chain"]
            RouteLocator --> CorsFilter["CORS Config Filter<br>(Verify Origin/Headers)"]
            CorsFilter --> AuthFilter["Authentication Filter<br>(JWT verification & claims injection)"]
            AuthFilter --> RateLimitFilter["Redis Rate Limiter Filter<br>(Token Bucket checks)"]
            RateLimitFilter --> CircuitBreaker["Resilience4j Gateway Filter<br>(Circuit Breaker & Fallback)"]
        end

        CircuitBreaker --> ProxyClient["HTTP Client Proxy<br>(Dispatches to Downstream Service)"]
    end

    %% External
    ProxyClient -->|Forwards to| Microservice["Downstream Microservice"]
    AuthFilter -->|Reads certificates| Keycloak["Keycloak JWKS Provider"]
    RateLimitFilter -->|Incr/Decr counters| Redis["Redis Instance"]

    classDef component fill:#1168BD,stroke:#0B4E8F,color:#fff;
    classDef ext fill:#999999,stroke:#666666,color:#fff;

    class Netty,RouteLocator,CorsFilter,AuthFilter,RateLimitFilter,CircuitBreaker,ProxyClient component;
    class Client,Keycloak,Redis,Microservice ext;
```

*   **Authentication Filter:** Parses the `Authorization: Bearer <JWT>` header, validates the cryptographic signature using Keycloak's JSON Web Key Sets (JWKS) keys, extracts tenant and user scopes, and inserts `X-Tenant-ID`, `X-User-ID`, and `X-User-Roles` headers.
*   **Redis Rate Limiter Filter:** Implements a token bucket algorithm to rate-limit requests per tenant and IP address.
*   **Circuit Breaker:** Wraps outbound calls to downstream microservices. If a service experiences downtime or high latency, the Gateway quickly drops the request and serves a graceful HTTP fallback response.

---

## 2. Organization Service (Hexagonal Architecture)

The Organization Service adheres to Hexagonal Architecture (Ports & Adapters) principles to isolate the core domain models from external database adapters, framework bindings, or message queues.

```mermaid
graph TD
    %% Driving Adapters (Input)
    OrgController["OrganizationController<br>(Spring REST Controller)"]
    RabbitConsumer["RabbitMQEventListenerAdapter<br>(Message Listener)"]

    subgraph HexagonalCore ["Organization Service Domain Core"]
        %% Input Ports
        subgraph InputPorts ["Driver Ports"]
            OrgUseCase["OrganizationUseCase<br>(Interface)"]
        end

        %% Domain Logic
        subgraph DomainModels ["Core Domain Logic & Models"]
            OrgService["OrganizationDomainService<br>(Business logic rules)"]
            OrgModel["Organization Entity<br>(Domain Entity)"]
            WorkspaceModel["Workspace Entity<br>(Domain Entity)"]
        end

        %% Output Ports
        subgraph OutputPorts ["Driven Ports"]
            OrgRepoPort["OrganizationRepositoryPort<br>(Interface)"]
            EventPubPort["EventPublisherPort<br>(Interface)"]
        end
    end

    %% Driven Adapters (Output)
    SpringDataRepo["PostgresJpaRepositoryAdapter<br>(Spring Data JPA / Tenant Routing)"]
    RabbitPublisher["RabbitMQPublisherAdapter<br>(RabbitTemplate publisher)"]

    %% Flow of control
    OrgController -->|Calls| OrgUseCase
    RabbitConsumer -->|Calls| OrgUseCase
    OrgUseCase -->|Implemented by| OrgService
    OrgService -->|Manipulates| OrgModel
    OrgService -->|Manipulates| WorkspaceModel
    OrgService -->|Uses| OrgRepoPort
    OrgService -->|Uses| EventPubPort

    OrgRepoPort -->|Implemented by| SpringDataRepo
    EventPubPort -->|Implemented by| RabbitPublisher

    SpringDataRepo -->|Executes query on| PostgresDB[("PostgreSQL DB (Tenant Schema)")]
    RabbitPublisher -->|Publishes to| RabbitMQ[("RabbitMQ Broker")]

    classDef port fill:#005E5D,stroke:#003E3E,color:#fff;
    classDef adapter fill:#1168BD,stroke:#0B4E8F,color:#fff;
    classDef model fill:#D85A2B,stroke:#993C18,color:#fff;
    classDef ext fill:#999999,stroke:#666666,color:#fff;

    class OrgUseCase,OrgRepoPort,EventPubPort port;
    class OrgController,RabbitConsumer,SpringDataRepo,RabbitPublisher adapter;
    class OrgService,OrgModel,WorkspaceModel model;
    class PostgresDB,RabbitMQ ext;
```

*   **Driver Adapters (Input):** `OrganizationController` receives HTTP REST requests, maps them into domain request commands, and triggers the `OrganizationUseCase` port.
*   **Domain Core:** Contains the core entity models (`Organization`, `Workspace`, `Team`) and business domain rules. It has no dependencies on Spring, Hibernate, or database engines.
*   **Driven Adapters (Output):** The repositories implement the outgoing ports. The `PostgresJpaRepositoryAdapter` uses tenant context-aware routing to direct statements to the tenant's isolated database schema.
