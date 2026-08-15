# Atlas SaaS Platform Accelerator

> Production-ready, Multi-Tenant Cloud-Native Backend Platform for Modern SaaS Applications.

Atlas SaaS Platform Accelerator provides a secure, scalable, enterprise-grade backend blueprint built using **Java 21** and **Spring Boot 3**. It handles all foundational capabilities required by modern SaaS applications, allowing developers to focus entirely on core business logic from day one.

---

## 🚀 Key Features

*   **Multi-Tenancy:** Schema-per-tenant data isolation dynamically routed using context-aware request attributes.
*   **Identity & Access Control (RBAC):** Centrally federated identity management via **Keycloak (OAuth2 / OIDC)** with offline JWT verification.
*   **API Gateway:** Stateless reactive routing proxy featuring rate limiting, circuit breaking, and context propagation.
*   **Reliable Messaging:** Transactional Outbox Pattern with RabbitMQ, complete with retries and Dead Letter Queues (DLQ).
*   **Three-Pillar Observability:** Micrometer metrics (Prometheus scraping), distributed tracing (Zipkin format to Tempo), and log MDC correlation filters (Loki) out-of-the-box.
*   **Continuous Integration / Deployment:** GitHub Actions pipelines checking Gradle compilation, testing, Terraform plans, Helm syntax validation, and matrix multi-service GHCR packaging.
*   **Production Hardening & HA:** Resilience4j circuit breakers/retries, tuned Hikari connection pools, graceful JVM shutdowns, and auto-scaling Kubernetes Helm templates (HPA, PDB, Quota).

---

## 🛠️ Technology Stack

*   **Language:** Java 21 (utilizing Virtual Threads)
*   **Framework:** Spring Boot 3 & Spring Cloud Gateway
*   **Identity Provider:** Keycloak
*   **Databases:** PostgreSQL
*   **Cache:** Redis
*   **Event Broker:** RabbitMQ
*   **Object Store:** MinIO (S3-compatible API)
*   **Observability:** Prometheus, Grafana, Loki, Tempo
*   **Orchestration:** Docker Compose & Kubernetes (Helm)
*   **Infrastructure:** Terraform

---

## 📁 Repository Structure

```text
atlas-saas-platform/
├── .github/
│   └── workflows/              # GitHub Actions CI, Infra, and Container Publish
├── gateway/
│   └── api-gateway/            # Reactive Gateway Ingress Controller
├── shared/
│   ├── shared-kernel/          # Core Exceptions & API Envelopes
│   ├── shared-security/        # ThreadLocal Tenant Contexts & RBAC Interceptors
│   └── shared-observability/   # Prometheus configs, Zipkin tracing, and MDC logs
├── services/
│   ├── organization-service/   # Hexagonal Core Organization API service
│   └── notification-service/   # Asynchronous Event Broker Consumer
├── infrastructure/
│   ├── docker/                 # Developer Compose Stack & SQL seed scripts
│   ├── observability/          # Prometheus, Loki, Tempo, and Grafana datasource configurations
│   ├── terraform/              # Cloud provisioning plans (VPC, EKS, RDS, Redis, S3)
│   └── helm/                   # Kubernetes deployment chart definitions
├── docs/                       # Comprehensive guides (Onboarding, APIs, Deployments)
├── gradlew
└── settings.gradle
```

---

## 💻 Getting Started (Local Development)

For detailed developer instructions, please review the local documentation guides:
*   [Developer Onboarding Guide](file:///c:/Users/ascora/Desktop/atlas-saas-platform/docs/developer-onboarding.md)
*   [API Specification & Gateway Guide](file:///c:/Users/ascora/Desktop/atlas-saas-platform/docs/api-specification.md)
*   [Deployment & Operations Runbook](file:///c:/Users/ascora/Desktop/atlas-saas-platform/docs/deployment-runbook.md)

### 1. Launch local infrastructure dependencies
Run the pre-configured local development stack containing PostgreSQL, Redis, RabbitMQ, MinIO, Keycloak, Prometheus, Grafana, Loki, and Tempo:

```bash
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

### 2. Build the project
To compile and build all modules using the Gradle wrapper:

```bash
./gradlew build -x test
```

### 3. Launch Services
You can run individual microservices in your favorite IDE or via command line:
```bash
./gradlew :gateway:api-gateway:bootRun
./gradlew :services:organization-service:bootRun
./gradlew :services:notification-service:bootRun
```
