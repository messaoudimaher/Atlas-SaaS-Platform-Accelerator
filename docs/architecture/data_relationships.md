# Architecture: Database Relationships & Tenant Isolation

This document outlines the database schemas, relationships, and the technical implementation of tenant isolation in the **Atlas SaaS Platform Accelerator**.

---

## 1. Multi-Tenant Database Isolation Model

Atlas implements the **Schema-per-Tenant** isolation model for the core Organization/Workspace service, and the **Logical Tenant Separation** (Shared Table with Tenant ID filter) for downstream metadata services (Billing, API Keys, File Metadata, Audit Logs).

```mermaid
graph TD
    subgraph OrganizationServiceDB ["Organization PostgreSQL Database"]
        subgraph Schema_Global ["public (Global Shared Schema)"]
            GlobalOrgRegistry["organizations_registry<br>[tenant_id, schema_name, status]"]
        end

        subgraph Schema_TenantA ["tenant_company_a (Isolated Schema)"]
            OrgsA["organizations"]
            WorkspacesA["workspaces"]
            TeamsA["teams"]
            MembersA["members"]
            InvitesA["invitations"]
            OutboxA["outbox_events"]
        end

        subgraph Schema_TenantB ["tenant_company_b (Isolated Schema)"]
            OrgsB["organizations"]
            WorkspacesB["workspaces"]
            TeamsB["teams"]
            MembersB["members"]
            InvitesB["invitations"]
            OutboxB["outbox_events"]
        end
    end

    OrgsA --> WorkspacesA
    WorkspacesA --> TeamsA
    OrgsA --> MembersA
    OrgsA --> InvitesA

    OrgsB --> WorkspacesB
    WorkspacesB --> TeamsB
    OrgsB --> MembersB
    OrgsB --> InvitesB
```

### Dynamic Tenant Context Routing
1.  **Request Ingestion:** The gateway validates the JWT, extracts `tenant_id`, and sets the header `X-Tenant-Id`.
2.  **ThreadLocal Injection:** A Spring interceptor intercepts the request downstream, reads the `X-Tenant-Id` header, and registers it to a `TenantContext` thread-local variable.
3.  **DataSource Routing:** Hibernate's `MultiTenantConnectionProvider` retrieves a connection, and `CurrentTenantIdentifierResolver` executes `SET search_path TO <tenant_schema_name>` dynamically before executing any query.

---

## 2. Microservice Entity-Relationship Diagrams (ERD)

### 1. Organization & Hierarchy Service Schema (Schema-per-Tenant)

Within each tenant's schema:

```mermaid
erDiagram
    organizations {
        uuid id PK
        varchar name
        varchar status
        timestamp created_at
        timestamp updated_at
    }

    workspaces {
        uuid id PK
        uuid organization_id FK
        varchar name
        varchar status
        timestamp created_at
    }

    teams {
        uuid id PK
        uuid workspace_id FK
        varchar name
        timestamp created_at
    }

    members {
        uuid id PK
        uuid organization_id FK
        uuid user_id
        varchar email
        varchar role
        timestamp joined_at
    }

    invitations {
        uuid id PK
        uuid organization_id FK
        varchar email
        varchar role
        varchar token UK
        varchar status
        timestamp expires_at
        timestamp created_at
    }

    outbox_events {
        uuid id PK
        varchar event_type
        varchar aggregate_type
        uuid aggregate_id
        text payload
        varchar status
        timestamp created_at
    }

    organizations ||--o{ workspaces : contains
    workspaces ||--o{ teams : contains
    organizations ||--o{ members : employs
    organizations ||--o{ invitations : registers
```

### 2. Subscription & Billing Service Schema

```mermaid
erDiagram
    subscriptions {
        uuid id PK
        uuid tenant_id UK
        varchar stripe_customer_id
        varchar stripe_subscription_id
        varchar plan_id
        varchar status
        timestamp current_period_start
        timestamp current_period_end
    }

    plan_limits {
        varchar plan_id PK
        varchar name
        integer max_users
        integer max_workspaces
        integer max_api_quota
        bigint max_storage_bytes
        varchar features
    }
```

### 3. API Key Service Schema

```mermaid
erDiagram
    api_keys {
        uuid id PK
        uuid tenant_id
        varchar name
        varchar key_hash UK
        varchar scopes
        varchar status
        timestamp expires_at
        timestamp created_at
    }
```

### 4. File Storage Service Schema

```mermaid
erDiagram
    file_metadata {
        uuid id PK
        uuid tenant_id
        varchar original_name
        varchar file_path
        varchar content_type
        bigint file_size
        uuid uploaded_by
        timestamp created_at
    }
```
