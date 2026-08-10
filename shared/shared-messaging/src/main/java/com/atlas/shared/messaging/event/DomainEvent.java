package com.atlas.shared.messaging.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public interface DomainEvent extends Serializable {
    UUID getEventId();
    String getEventType();
    Instant getOccurredOn();
    String getTenantId();
}
