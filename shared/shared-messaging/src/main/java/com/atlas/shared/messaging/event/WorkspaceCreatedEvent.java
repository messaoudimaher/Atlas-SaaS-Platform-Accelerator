package com.atlas.shared.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceCreatedEvent(
        UUID eventId,
        UUID workspaceId,
        UUID organizationId,
        String name,
        String tenantId,
        Instant occurredOn
) implements DomainEvent {

    public static final String EVENT_TYPE = "WorkspaceCreated";

    public WorkspaceCreatedEvent(UUID workspaceId, UUID organizationId, String name, String tenantId) {
        this(UUID.randomUUID(), workspaceId, organizationId, name, tenantId, Instant.now());
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public Instant getOccurredOn() {
        return occurredOn;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }
}
