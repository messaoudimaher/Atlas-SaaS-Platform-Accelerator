# Architecture: Deployment (C4 Level 4)

This document describes the physical deployment architecture (C4 Level 4) for the **Atlas SaaS Platform Accelerator** deployed to a public cloud environment (AWS) using Kubernetes (EKS).

---

## 1. Physical Deployment Diagram

```mermaid
graph TD
    User["SaaS User Client"] -->|HTTPS / DNS| Route53["AWS Route 53<br>(DNS Gateway)"]

    subgraph AWS_VPC ["AWS VPC (Virtual Private Cloud)"]
        
        subgraph Public_Subnet ["Public Subnet (DMZ)"]
            Route53 --> ALB["AWS Application Load Balancer (ALB)<br>[TLS Termination]"]
        end

        subgraph Private_Subnet ["Private Subnet (EKS Cluster Workers)"]
            ALB --> IngressController["Kubernetes Nginx Ingress Controller"]

            subgraph EKS_Cluster ["Amazon EKS Cluster Node Group"]
                IngressController -->|Routes traffic| GatewayPod["Gateway Pods<br>(Spring Cloud Gateway replica)"]

                %% Pod Replica groups
                subgraph AppPods ["Microservice Pod Replicas"]
                    GatewayPod --> OrgPods["Organization Pods<br>(HPA Enabled)"]
                    GatewayPod --> BillingPods["Billing Pods<br>(HPA Enabled)"]
                    GatewayPod --> KeycloakPods["Keycloak Auth Pods"]
                end
            end
        end

        subgraph Database_Subnet ["Database Private Subnet (Data Tier)"]
            OrgPods --> RDS_Postgres[("Amazon Aurora PostgreSQL RDS<br>(Multi-AZ / Isolated Schemas)")]
            BillingPods --> RDS_Postgres
            KeycloakPods --> KeycloakRDS[("Identity DB Instance")]

            GatewayPod --> Redis_Cluster[("Amazon ElastiCache Redis<br>(Clustered Node Setup)")]
            OrgPods --> Redis_Cluster

            OrgPods --> RabbitMQ_Broker[("Amazon MQ / RabbitMQ Cluster")]
            BillingPods --> RabbitMQ_Broker
        end

        subgraph S3_Boundary ["Cloud Object Store Layer"]
            OrgPods --> S3["AWS S3 / MinIO Storage Bucket"]
        end
    end

    classDef aws fill:#FF9900,stroke:#D68100,color:#fff;
    classDef k8s fill:#326CE5,stroke:#2651AD,color:#fff;
    classDef data fill:#005E5D,stroke:#003E3E,color:#fff;
    classDef client fill:#08427B,stroke:#052E56,color:#fff;

    class Route53,ALB,RDS_Postgres,KeycloakRDS,Redis_Cluster,RabbitMQ_Broker,S3 aws;
    class IngressController,GatewayPod,OrgPods,BillingPods,KeycloakPods k8s;
    class User client;
```

---

## 2. Infrastructure Node Layout

### 1. Networking (VPC Setup)
*   **VPC Partitioning:** Divided across three Availability Zones (AZs) for high availability.
*   **Subnet Separation:**
    *   **Public Subnets:** Houses the public-facing AWS Application Load Balancers (ALBs) and NAT Gateways.
    *   **Private Subnets:** Houses EKS worker nodes. No public IP addresses are assigned to worker instances; all outbound traffic passes through the NAT Gateway.
    *   **Isolated Database Subnets:** Enforces security by isolating databases, preventing all direct traffic except from authorized microservice security groups.

### 2. Orchestration Tier (Amazon EKS)
*   **Nginx Ingress Controller:** Receives routed requests from the AWS ALB, matches host headers, and routes requests to the `gateway-service` Kubernetes ClusterIP.
*   **Horizontal Pod Autoscaler (HPA):** Monitors CPU and memory utilization metrics. Scale actions spin up additional pod replicas when utilization crosses a 75% threshold.
*   **Graceful Termination:** Pods are configured with a `preStop` hook to complete pending transactions and close open database connections gracefully before terminating.

### 3. Data Tier
*   **Amazon Aurora PostgreSQL:** Implements database replication across multiple Availability Zones. Primary node handles writes; read replicas handle scaling queries.
*   **Amazon ElastiCache Redis:** Configured with replication nodes across AZs, serving rate limiter and session storage caching.
*   **Amazon MQ (RabbitMQ):** Fully managed, clustered message broker instance supporting transactional outbox delivery logs.
