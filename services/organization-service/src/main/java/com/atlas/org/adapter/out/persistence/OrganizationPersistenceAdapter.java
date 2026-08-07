package com.atlas.org.adapter.out.persistence;

import com.atlas.org.domain.model.Organization;
import com.atlas.org.domain.model.Workspace;
import com.atlas.org.domain.port.out.OrganizationRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrganizationPersistenceAdapter implements OrganizationRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(OrganizationPersistenceAdapter.class);

    private final OrganizationJpaRepository organizationRepo;
    private final WorkspaceJpaRepository workspaceRepo;
    private final JdbcTemplate jdbcTemplate;

    public OrganizationPersistenceAdapter(
            OrganizationJpaRepository organizationRepo,
            WorkspaceJpaRepository workspaceRepo,
            JdbcTemplate jdbcTemplate) {
        this.organizationRepo = organizationRepo;
        this.workspaceRepo = workspaceRepo;
        this.jdbcTemplate = jdbcTemplate;
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
