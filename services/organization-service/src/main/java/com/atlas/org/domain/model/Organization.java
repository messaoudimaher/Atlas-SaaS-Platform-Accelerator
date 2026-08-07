package com.atlas.org.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Organization {
    private final UUID id;
    private String name;
    private String status;
    private final Instant createdAt;

    public Organization(UUID id, String name, String status, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Organization ID cannot be null");
        this.setName(name);
        this.setStatus(status);
        this.createdAt = Objects.requireNonNull(createdAt, "Creation timestamp cannot be null");
    }

    public static Organization createNew(String name) {
        return new Organization(
                UUID.randomUUID(),
                name,
                "ACTIVE",
                Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Organization name cannot be empty");
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
