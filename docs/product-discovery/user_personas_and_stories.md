# Product Discovery: User Personas and User Stories

This document defines the primary target audience, detailed personas, and the core user stories with acceptance criteria for the **Atlas SaaS Platform Accelerator**.

---

## 1. User Personas

### Persona A: Sarah (Start-up CTO)
*   **Background:** Sarah is launching an AI-powered logistics SaaS. She has a team of 3 developers and needs to deliver a secure, multi-tenant MVP to early pilot customers within 4 weeks.
*   **Pain Points:** 
    *   Wants to avoid wasting precious developer hours building auth, billing, and org management.
    *   Fear of failing security reviews from prospective enterprise pilot clients.
*   **Need:** A solid, secure, ready-to-run backend architecture in a scalable enterprise framework (Spring Boot / Kubernetes) so her team can focus entirely on the AI logistics logic.

### Persona B: David (Enterprise IT Administrator)
*   **Background:** David manages compliance and security audits at a large enterprise adopting various SaaS tools.
*   **Pain Points:**
    *   Rejects software that does not support OAuth2/OIDC or SAML SSO.
    *   Demands strict data isolation, audit logging of all configuration changes, and secure webhook integrations.
*   **Need:** Absolute assurance that the SaaS vendor's platform isolates their tenant data at the database level and logs every administrative event.

---

## 2. Epics & User Stories

### Epic 1: Identity & Tenant Management

#### User Story 1.1: Tenant Organization Provisioning
*   **As a** newly registering SaaS customer
*   **I want to** create a new Organization account during signup
*   **So that** I can host my team and projects in an isolated environment.
*   **Acceptance Criteria:**
    *   Given a valid request with organization name, domain, and owner details, the system creates the organization.
    *   When the organization is created, the system generates a unique, immutable UUID for the tenant.
    *   A transactional outbox event `OrganizationCreated` must be generated.

#### User Story 1.2: Organization Member Invitations
*   **As an** Organization Admin
*   **I want to** invite new users to join the organization via email
*   **So that** they can collaborate on projects within our organization.
*   **Acceptance Criteria:**
    *   The invite endpoint must generate a secure, cryptographic invitation token with a 72-hour expiration TTL.
    *   The email notification is queued as an event `InvitationSent` containing the token link.
    *   Accepting the invitation links the user to the organization with the specified role and logs an audit trail event.

---

### Epic 2: Authentication & Authorization

#### User Story 2.1: Secure OAuth2/JWT Authentication
*   **As a** registered SaaS User
*   **I want to** log in securely using OpenID Connect (OIDC) through Keycloak
*   **So that** my credentials are secure and I receive a valid JWT for accessing resource endpoints.
*   **Acceptance Criteria:**
    *   Requests with missing or invalid tokens return `401 Unauthorized` at the Gateway.
    *   Successful authentication returns a JWT containing the user's details, active tenant ID, and permissions.
    *   Gateway propagates claims as downstream HTTP headers (`X-Tenant-Id`, `X-User-Id`, `X-User-Roles`).

#### User Story 2.2: API Key Access
*   **As a** developer integrating with the SaaS platform
*   **I want to** generate secure API keys
*   **So that** my scripts can programmatically interact with APIs without interactive user authentication.
*   **Acceptance Criteria:**
    *   The system generates API keys in format `prefix_key` with a secure random hash.
    *   Only the cryptographic hash is stored in the database; the raw key is shown only once upon creation.
    *   The API key validation check must authenticate the tenant, check active plan quotas, and enforce rate limits.

---

### Epic 3: Subscriptions and Plan Enforcement

#### User Story 3.1: Tier-Based Quota Limits
*   **As a** tenant administrator
*   **I want to** be restricted or upgraded based on my subscription limits (Starter vs. Professional vs. Enterprise)
*   **So that** the platform restricts consumption (number of users, workspaces, API calls) based on our pricing tier.
*   **Acceptance Criteria:**
    *   If a Starter tenant attempts to create a 3rd workspace, the Workspace Service returns a `403 Forbidden` response indicating quota exceeded.
    *   If a tenant exceeds their monthly API request limits, the Gateway returns `429 Too Many Requests`.
    *   Changing a plan generates a transactional event `SubscriptionChanged` which triggers real-time updates of tenant quota rules in Redis caches.
