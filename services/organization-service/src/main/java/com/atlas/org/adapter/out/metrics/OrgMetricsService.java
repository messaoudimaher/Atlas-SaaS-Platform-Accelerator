package com.atlas.org.adapter.out.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OrgMetricsService {

    private final Counter orgCreatedCounter;
    private final Counter workspaceCreatedCounter;

    public OrgMetricsService(MeterRegistry registry) {
        this.orgCreatedCounter = Counter.builder("atlas.organizations.created")
                .description("Total number of organizations created")
                .register(registry);

        this.workspaceCreatedCounter = Counter.builder("atlas.workspaces.created")
                .description("Total number of workspaces created")
                .register(registry);
    }

    public void incrementOrgCreated() {
        orgCreatedCounter.increment();
    }

    public void incrementWorkspaceCreated() {
        workspaceCreatedCounter.increment();
    }
}
