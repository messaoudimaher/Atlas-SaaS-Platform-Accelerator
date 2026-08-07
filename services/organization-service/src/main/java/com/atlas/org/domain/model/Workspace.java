package com.atlas.org.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Workspace {
    private final UUID id;
    private final UUID organizationId;
    private String name;
    private String status;
    private final Instant createdAt;

    public Workspace(UUID id, UUID organizationId, String name, String status, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Workspace ID cannot be null");
        this.organizationId = Objects.requireNonNull(organizationId, "Organization ID reference cannot be null");
        this.setName(name);
        this.setStatus(status);
        this.createdAt = Objects.requireNonNull(createdAt, "Creation timestamp cannot be null");
    }

    public static Workspace createNew(UUID organizationId, String name) {
        return new Workspace(
                UUID.randomUUID(),
                organizationId,
                name,
                "ACTIVE",
                Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Workspace name cannot be empty");
        }
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
