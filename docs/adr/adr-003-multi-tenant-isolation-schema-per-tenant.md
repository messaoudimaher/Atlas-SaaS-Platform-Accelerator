# Architectural Decision Record: ADR-003 - Multi-Tenant Isolation Strategy

## Status
Accepted

## Date
2026-07-28

## Context
Data privacy and isolation are critical requirements for B2B SaaS products. Enterprise customers demand assurances that their data cannot be leaked, accessed, or corrupted by other tenants on the platform.

### Alternatives Considered
1.  **Shared Database, Shared Table (Logical Isolation):**
    *   *Pros:* Low infrastructure cost, easy to upgrade schemas, query all data easily.
    *   *Cons:* High risk of accidental data leakage if developer forgets `where tenant_id = ?` clause; hard to comply with strict enterprise data audits.
2.  **Database-per-Tenant (Physical Isolation):**
    *   *Pros:* Maximum security isolation, custom backups/restores per tenant, databases can reside in different geographical regions.
    *   *Cons:* Very high resource overhead (hundreds of database connection pools), complex schema migrations, high cost for small startup tenants.
3.  **Shared Database, Schema-per-Tenant (Namespace Isolation):**
    *   *Pros:* Strong logical isolation (PostgreSQL schemas), database connection pool sharing, easy schema updates while maintaining separate table namespaces.
    *   *Cons:* Schema migration scripts must run against all schemas; maximum number of tables in a single DB instance is bounded.

---

## Decision
We choose a **hybrid tenant isolation model** with **Schema-per-Tenant** as the primary strategy for core transactional microservices (Organization, Workspace, Teams, Members) and **Shared Database, Shared Table (Logical Isolation)** for high-volume telemetry/metadata services (Audit Logs, File Metadata, API keys).

*   **Core Entities:** Placed in individual tenant PostgreSQL schemas. This guarantees that SQL commands run under separate namespace boundaries (e.g., `tenant_a.workspaces` vs `tenant_b.workspaces`), preventing cross-tenant querying bugs.
*   **Telemetry and Utility Services:** Placed in shared tables with tenant_id indexes to conserve database resource pools and minimize system maintenance complexity.

---

## Consequences

### Positive
*   **Data Security:** Enforces strict data segregation, satisfying enterprise compliance audits.
*   **Infrastructure Efficiency:** Shares a single database engine instance across multiple tenants during startup, reducing deployment costs.
*   **Spring Support:** Spring Data JPA dynamically resolves schemas at connection execution time using a request interceptor thread context.

### Negative
*   **Migration Overhead:** Liquibase/Flyway database migrations must be executed sequentially or in parallel across all active tenant schemas at startup.
*   **Resource Monitoring:** Requires careful monitoring of database resources (disk I/O and CPU) as multiple schemas exist in the same database instance.
