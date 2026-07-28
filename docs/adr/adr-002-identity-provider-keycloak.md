# Architectural Decision Record: ADR-002 - Identity Provider Selection

## Status
Accepted

## Date
2026-07-28

## Context
A multi-tenant SaaS application requires robust identity federations, user registries, multi-factor authentication (MFA), password security policies, role mappings, and SSO capabilities. Building these capabilities from scratch is high-risk and violates security guidelines.

### Alternatives Considered
*   **Custom Authentication Service:** Writing standard database hashing algorithms (e.g., BCrypt) and token generators in-house. This increases security vulnerabilities, compliance auditing risks, and delays time-to-market.
*   **SaaS IDPs (Auth0 / Clerk):** Extremely simple to integrate, but introduces third-party dependency, vendor lock-in, and unpredictable pricing curves scaling with monthly active users (MAUs).
*   **Keycloak Identity Provider:** Free, open-source, compliant with OIDC, OAuth2, and SAML, highly configurable, and self-hostable in our Kubernetes cluster.

---

## Decision
We select **Keycloak** as the central Identity Provider (IdP) for the Atlas SaaS Platform Accelerator.

*   **Federation and Standards:** Out-of-the-box support for OpenID Connect (OIDC) and OAuth2 flows.
*   **Self-Hostable:** Fits directly into our Kubernetes deployment model, ensuring that tenant identity databases remain fully under the enterprise owner's control.
*   **Tenant Mapping:** Supports multiple realms or unified realms with custom attributes to cleanly map users to their tenant org IDs.

---

## Consequences

### Positive
*   **Security Compliance:** Ensures passwords, MFA tokens, and social sessions are managed by a production-tested security platform.
*   **Offline Validation:** The Spring Cloud Gateway can perform cryptographic JWT validation locally using Keycloak's public keys, omitting round-trip network authentication overhead.
*   **SSO Ready:** Easily maps external enterprise credentials (SAML/Active Directory) for enterprise tenants.

### Negative
*   **Deployment Overhead:** Running a clustered, HA Keycloak instance in production requires additional resource allocations (CPU/Memory) and database backend configurations.
*   **Keycloak Administration:** Configuring realms, themes, and roles requires specialized administrative knowledge.
