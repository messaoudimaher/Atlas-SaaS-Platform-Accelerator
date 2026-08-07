# Atlas SaaS Platform Accelerator

> Production-ready, Multi-Tenant Cloud-Native Backend Platform for Modern SaaS Applications.

Atlas SaaS Platform Accelerator provides a secure, scalable, enterprise-grade backend blueprint built using **Java 21** and **Spring Boot 3**. It handles all foundational capabilities required by modern SaaS applications, allowing developers to focus entirely on core business logic from day one.

---

## 🚀 Key Features

*   **Multi-Tenancy:** Schema-per-tenant data isolation dynamically routed using context-aware request attributes.
*   **Identity & Access Control (RBAC):** Centrally federated identity management via **Keycloak (OAuth2 / OIDC)** with offline JWT verification.
*   **API Gateway:** Stateless reactive routing proxy featuring rate limiting, circuit breaking, and context propagation.
*   **Reliable Messaging:** Transactional Outbox Pattern with RabbitMQ, complete with retries and Dead Letter Queues (DLQ).
*   **Infrastructure-as-Code:** Production-ready Terraform deployment scripts and parameterized Helm charts.

---

## 🛠️ Technology Stack

*   **Language:** Java 21 (utilizing Virtual Threads)
*   **Framework:** Spring Boot 3 & Spring Cloud Gateway
*   **Identity Provider:** Keycloak
*   **Databases:** PostgreSQL
*   **Cache:** Redis
*   **Event Broker:** RabbitMQ
*   **Object Store:** MinIO (S3-compatible API)
*   **Orchestration:** Docker Compose & Kubernetes (Helm)
*   **Infrastructure:** Terraform

---

## 📁 Repository Structure

```text
atlas-saas-platform/
├── gateway/
│   └── api-gateway/            # Reactive Gateway Ingress Controller
├── shared/
│   ├── shared-kernel/          # Core Exceptions & API Envelopes
│   └── shared-security/        # ThreadLocal Tenant Contexts & Interceptors
├── infrastructure/
│   ├── docker/                 # Developer Compose Stack & SQL seed scripts
│   ├── terraform/              # Provisioning plans (VPC, EKS, RDS, Redis, S3)
│   └── helm/                   # Kubernetes deployment chart definitions
├── gradlew
└── settings.gradle
```

---

## 💻 Getting Started (Local Development)

### Prerequisites

*   Java 21 JDK
*   Docker & Docker Compose

### 1. Launch local infrastructure dependencies
Run the pre-configured local development stack containing PostgreSQL, Redis, RabbitMQ, MinIO, and Keycloak:

```bash
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

Postgres databases are automatically seeded on launch using [init-db.sql](file:///c:/Users/ascora/Desktop/atlas-saas-platform/infrastructure/docker/init-db.sql), and Keycloak auto-imports the configuration defined in [keycloak-realm.json](file:///c:/Users/ascora/Desktop/atlas-saas-platform/infrastructure/docker/keycloak-realm.json).

### 2. Build the project
To compile and build all modules using the Gradle wrapper:

```bash
./gradlew build -x test
```

### 3. Expose gateway APIs
The API Gateway boots on port `8080`. Send authenticated requests through the gateway using the Keycloak OIDC client configurations.
