# Changelog

All notable changes to the Atlas SaaS Platform Accelerator will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.2.0] - 2026-07-28
### Added
- Completed **Phase 2: Architecture Design**.
- Created C4 Level 1-4 Architecture Specifications under `docs/architecture/`:
  - `system_context.md` (System boundaries and actors)
  - `containers.md` (Microservice layout, datastores, and message brokers)
  - `components.md` (Gateway filters and hexagonal ports/adapters breakdown)
  - `deployment.md` (Kubernetes EKS cluster layout)
- Added Database entity relations and multi-tenant schema isolation logic to `data_relationships.md`.
- Added Authentication and Transactional Outbox sequence diagrams to `flows.md`.
- Created initial Architectural Decision Records (ADRs) under `docs/adr/`:
  - `adr-001-microservices-framework-spring-boot.md`
  - `adr-002-identity-provider-keycloak.md`
  - `adr-003-multi-tenant-isolation-schema-per-tenant.md`
  - `adr-004-transactional-outbox-pattern.md`

## [0.1.0] - 2026-07-28
### Added
- Completed **Phase 1: Product Discovery**.
- Created `PROJECT_STATUS.md`, `ROADMAP.md`, and initial `CHANGELOG.md` in workspace root.
- Created Product Discovery Suite under `docs/product-discovery/`:
  - `vision_and_goals.md` (Tagline, Mission, Market Need, Competitor analysis)
  - `requirements.md` (Functional & Non-Functional specifications, plan limits)
  - `user_personas_and_stories.md` (User personas, Epics, user stories with acceptance criteria)
  - `backlog_and_milestones.md` (Product backlog, Story point estimates, sprint goals)
  - `risk_analysis.md` (Operational and architecture risks with mitigation plans)
