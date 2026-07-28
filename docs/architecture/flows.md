# Architecture: Request & Message Flows

This document details the request execution paths, authentication lifecycle, and transactional messaging flows of the **Atlas SaaS Platform Accelerator**.

---

## 1. Authentication & JWT Authorization Lifecycle

This flow illustrates how a client signs in, obtains a JWT from Keycloak, and uses it to call tenant-restricted API endpoints.

```mermaid
sequenceDiagram
    autonumber
    actor Client as SPA / API Client
    participant GW as Spring Cloud Gateway
    participant Keycloak as Keycloak OIDC
    participant Service as Downstream Service (e.g. Org Service)

    %% Step 1: Login
    Client->>Keycloak: Post /auth/realms/atlas/protocol/openid-connect/token (username/password)
    activate Keycloak
    Note over Keycloak: Validates credentials, checks RBAC mappings,<br/>injects tenant_id into JWT custom claims.
    Keycloak-->>Client: Return Tokens (Access Token JWT, Refresh Token)
    deactivate Keycloak

    %% Step 2: Accessing APIs
    Client->>GW: GET /api/v1/organizations (Authorization: Bearer <JWT>)
    activate GW
    Note over GW: Validates JWT signature offline using cached JWKS public key.<br/>Extracts tenant_id, user_id, and roles.
    GW->>GW: Verify global & tenant-specific Rate Limiters
    
    %% Forwarding Context Headers
    GW->>Service: Forward request with headers:<br/>X-Tenant-Id, X-User-Id, X-User-Roles
    activate Service
    Note over Service: Interceptor sets TenantContext (ThreadLocal).<br/>Spring Security matches Roles against method annotations.
    Service-->>GW: Return HTTP 200 OK Response
    deactivate Service
    GW-->>Client: Forward response to client
    deactivate GW
```

---

## 2. Transactional Outbox Pattern & Event Flow

To maintain consistent state without distributed transactions (avoiding 2PC/XA), Atlas uses the Transactional Outbox Pattern.

```mermaid
sequenceDiagram
    autonumber
    participant Client as Client Request
    participant Controller as Controller / UseCase
    participant DB as Postgres (Tenant Schema)
    participant Broker as RabbitMQ Event Bus
    participant Publisher as Outbox Publisher Daemon
    participant Consumer as Notification Service Consumer

    %% Database Transaction
    Client->>Controller: Create Workspace ("Dev Workspace")
    activate Controller
    Controller->>DB: Begin DB Transaction
    Controller->>DB: Insert Workspace Record
    Controller->>DB: Insert Outbox Event Record (Status: PENDING)
    Controller->>DB: Commit Transaction
    Note over DB: Atomically commits both workspace and outbox records.
    Controller-->>Client: Return HTTP 201 Created
    deactivate Controller

    %% Outbox Publisher Loop
    activate Publisher
    loop Every 500ms
        Publisher->>DB: Query Outbox events where status = PENDING
        DB-->>Publisher: Returns event list
        loop For each event
            Publisher->>Broker: Publish message (routing_key: workspace.created)
            Broker-->>Publisher: Acknowledge publication receipt
            Publisher->>DB: Update Outbox event status = PROCESSED
        end
    end
    deactivate Publisher

    %% Asynchronous Consumption
    activate Consumer
    Broker->>Consumer: Deliver message: WorkspaceCreated
    Consumer->>Consumer: Process message (Idempotent check by event uuid)
    Consumer->>Consumer: Trigger side effect (Send email notification)
    Consumer-->>Broker: Acknowledge message (ACK)
    deactivate Consumer
```
