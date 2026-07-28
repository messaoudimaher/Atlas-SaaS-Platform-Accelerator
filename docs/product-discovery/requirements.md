# Product Discovery: Functional and Non-Functional Requirements

This document outlines the detailed system requirements, tenant isolation strategies, and pricing plan details for the **Atlas SaaS Platform Accelerator**.

---

## 1. Functional Requirements (FR)

### FR1: API Gateway
*   **Routing:** Dynamically route requests to downstream microservices based on URL path patterns.
*   **Rate Limiting:** Implement global and tenant-specific rate limiting at the gateway using Redis.
*   **Authentication Validation:** Intercept requests, validate JWT tokens from Keycloak, and inject tenant and user context headers into downstream requests.
*   **CORS & Security Headers:** Apply global security headers and CORS configurations.

### FR2: Identity and Role-Based Access Control (RBAC)
*   **Single Sign-On (SSO):** Integrate Keycloak as the Identity Provider (IdP) supporting OpenID Connect (OIDC) and OAuth2.
*   **Multi-Tenancy Auth:** Associate users with one or more Organizations and roles (Owner, Admin, Member, Guest).
*   **Custom Claims:** Inject `tenant_id` and role permissions into JWT claims to allow downstream self-contained token validation.
*   **API Key Management:** Support programmatic access via API keys with tenant validation, scope restrictions, and cryptographic hashing.

### FR3: Tenant & Hierarchy Management
*   **Organization Service:** Provide endpoints to register, update, and manage organizations.
*   **Workspace Service:** Support division of an organization into multiple workspaces (e.g., Development, Production).
*   **Team Service:** Organize users within organizations/workspaces into named teams.
*   **Invitations:** Implement a robust email invitation flow with token validation and expiration.

### FR4: Event-Driven Core & Messaging
*   **Event Broker:** Publish business events to RabbitMQ (e.g., `UserRegistered`, `SubscriptionChanged`).
*   **Outbox Pattern:** Ensure transactional consistency by writing event payloads to an Outbox table in the service database before publishing them.
*   **Reliability:** Implement retry mechanism, Dead Letter Queues (DLQ), and idempotent consumption to handle network partitions or failures.

### FR5: Subscription and Billing Abstraction
*   **Billing Abstraction:** Define a standard `BillingProvider` interface.
*   **Providers:** Implement a `MockBillingProvider` for test scenarios and a `StripeBillingProvider` skeleton for future Stripe integration.
*   **Plan Enforcement:** Restrict features, API quotas, and storage capacities in real-time based on the organization's subscription tier.

### FR6: File Storage Service
*   **S3 Abstraction:** Upload, download, and delete assets using a MinIO-based S3 compatible storage interface.
*   **Metadata Tracking:** Store file attributes (size, mime-type, owner, organization) in the metadata database.
*   **Security:** Verify file types, enforce maximum upload sizes, and support pre-signed URLs for secure private file access.

### FR7: Observability and Audit Log Service
*   **Audit Service:** Write immutable audit records for sensitive actions (e.g., subscription upgrades, role modifications, configuration edits).
*   **Tracing:** Propagate unique correlation IDs across all service boundaries via HTTP headers.
*   **Metrics:** Collect JVM, application, and business-level metrics, and expose them in Prometheus format.

---

## 2. Non-Functional Requirements (NFR)

### NFR1: Scalability & Performance
*   **Horizontally Scalable:** Microservices must be stateless to support Kubernetes horizontal autoscaling (HPA) based on CPU/memory usage.
*   **Low Latency Gateway:** Gateway overhead must remain below 10ms for p99 requests.
*   **Caching Strategy:** Cache frequently accessed data (session state, tenant configs, rate limits) in Redis with appropriate TTL policies.

### NFR2: Security & Isolation
*   **Database Multi-Tenancy:** Implement a **Database-per-tenant** or **Schema-per-tenant** model to satisfy strict enterprise data boundaries. Avoid shared table structures with simple filter keys for sensitive business entities.
*   **Secured Secrets:** Absolutely no passwords, API keys, or certificates should exist in the repository code or ConfigMaps. Use environment variables mapped to Kubernetes Secrets.
*   **Vulnerability Scanning:** Code must pass Trivy scans for containers, Gitleaks for secrets, and OWASP Dependency Check for dependencies.

### NFR3: Availability & Fault Tolerance
*   **High Availability:** Infrastructure must have no single point of failure (replica sets for Postgres, cluster mode for Redis, mirrored queues in RabbitMQ).
*   **Graceful Degradation:** Use circuit breakers (Resilience4j) at the Gateway to isolate failing microservices.

---

## 3. Subscription Plans and Limits

The platform supports three distinct tier levels:

| Tier Features | Starter | Professional | Enterprise |
| :--- | :--- | :--- | :--- |
| **Max Users** | 5 users | 50 users | Unlimited |
| **Max Workspaces** | 2 workspaces | 10 workspaces | Unlimited |
| **Monthly API Quota** | 10,000 requests | 250,000 requests | Unlimited |
| **Max Storage** | 1 GB | 50 GB | Custom / Unlimited |
| **Advanced RBAC** | ❌ Basic roles | ✅ Fully customizable | ✅ Custom + SSO (SAML) |
| **Webhooks** | ❌ No | ✅ Up to 5 endpoints | ✅ Unlimited |
| **Support SLA** | Community | Email (24-hour response) | Dedicated (Chat/Phone 99.9% SLA) |
| **Price** | $29 / month | $149 / month | Custom Enterprise Pricing |
