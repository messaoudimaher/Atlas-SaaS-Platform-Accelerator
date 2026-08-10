package com.atlas.org.adapter.out.persistence;

import com.atlas.org.domain.model.Organization;
import com.atlas.org.domain.model.Workspace;
import com.atlas.org.domain.port.out.OrganizationRepositoryPort;
import com.atlas.shared.messaging.event.OrganizationCreatedEvent;
import com.atlas.shared.messaging.event.WorkspaceCreatedEvent;
import com.atlas.shared.messaging.outbox.OutboxEvent;
import com.atlas.shared.messaging.outbox.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class OrganizationPersistenceAdapter implements OrganizationRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(OrganizationPersistenceAdapter.class);

    private final OrganizationJpaRepository organizationRepo;
    private final WorkspaceJpaRepository workspaceRepo;
    private final OutboxEventRepository outboxEventRepo;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public OrganizationPersistenceAdapter(
            OrganizationJpaRepository organizationRepo,
            WorkspaceJpaRepository workspaceRepo,
            OutboxEventRepository outboxEventRepo,
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper) {
        this.organizationRepo = organizationRepo;
        this.workspaceRepo = workspaceRepo;
        this.outboxEventRepo = outboxEventRepo;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Organization save(Organization organization) {
        OrganizationJpaEntity entity = new OrganizationJpaEntity();
        entity.setId(organization.getId());
        entity.setName(organization.getName());
        entity.setStatus(organization.getStatus());
        entity.setCreatedAt(organization.getCreatedAt());
        
        OrganizationJpaEntity saved = organizationRepo.save(entity);

        // Transactional Outbox: Insert event in same transaction
        OrganizationCreatedEvent event = new OrganizationCreatedEvent(
                organization.getId(),
                organization.getName(),
                organization.getId().toString()
        );
        saveOutboxEvent("Organization", organization.getId(), event.getEventType(), event);

        return mapToDomain(saved);
    }

    @Override
    @Transactional
    public Workspace saveWorkspace(Workspace workspace) {
        WorkspaceJpaEntity entity = new WorkspaceJpaEntity();
        entity.setId(workspace.getId());
        entity.setOrganizationId(workspace.getOrganizationId());
        entity.setName(workspace.getName());
        entity.setStatus(workspace.getStatus());
        entity.setCreatedAt(workspace.getCreatedAt());

        WorkspaceJpaEntity saved = workspaceRepo.save(entity);

        // Transactional Outbox: Insert event in same transaction
        WorkspaceCreatedEvent event = new WorkspaceCreatedEvent(
                workspace.getId(),
                workspace.getOrganizationId(),
                workspace.getName(),
                workspace.getOrganizationId().toString()
        );
        saveOutboxEvent("Workspace", workspace.getId(), event.getEventType(), event);

        return mapToDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> findById(UUID id) {
        return organizationRepo.findById(id).map(this::mapToDomain);
    }

    @Override
    @Transactional
    public void createTenantSchema(UUID tenantId) {
        String schemaName = "tenant_" + tenantId.toString().replace("-", "_");
        log.info("Creating dynamic database schema for tenant: {}", schemaName);

        // Execute dynamic DDL commands under system security checks
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);

        // Create isolated organizations metadata table within tenant schema
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".organizations (" +
                "id UUID PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "created_at TIMESTAMP NOT NULL" +
                ")");

        // Create isolated workspaces table within tenant schema
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + schemaName + ".workspaces (" +
                "id UUID PRIMARY KEY, " +
                "organization_id UUID NOT NULL, " +
                "name VARCHAR(255) NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "created_at TIMESTAMP NOT NULL" +
                ")");

        // Ensure outbox_events table exists in public schema for shared polling
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS outbox_events (" +
                "id UUID PRIMARY KEY, " +
                "aggregate_type VARCHAR(100) NOT NULL, " +
                "aggregate_id UUID NOT NULL, " +
                "event_type VARCHAR(100) NOT NULL, " +
                "payload TEXT NOT NULL, " +
                "status VARCHAR(20) NOT NULL, " +
                "created_at TIMESTAMP NOT NULL, " +
                "processed_at TIMESTAMP, " +
                "retry_count INT NOT NULL DEFAULT 0" +
                ")");
    }

    private void saveOutboxEvent(String aggregateType, UUID aggregateId, String eventType, Object eventPayload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(eventPayload);
            OutboxEvent outbox = OutboxEvent.createPending(aggregateType, aggregateId, eventType, payloadJson);
            outboxEventRepo.save(outbox);
            log.debug("Outbox event saved: {} for aggregate: {}", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize domain event: {}", eventType, e);
            throw new IllegalStateException("Domain event serialization failed", e);
        }
    }

    private Organization mapToDomain(OrganizationJpaEntity entity) {
        return new Organization(
                entity.getId(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    private Workspace mapToDomain(WorkspaceJpaEntity entity) {
        return new Workspace(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getName(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
