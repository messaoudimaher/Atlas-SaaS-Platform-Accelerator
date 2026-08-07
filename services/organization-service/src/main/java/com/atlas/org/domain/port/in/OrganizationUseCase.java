package com.atlas.org.domain.port.in;

import com.atlas.org.domain.model.Organization;
import com.atlas.org.domain.model.Workspace;

import java.util.UUID;

public interface OrganizationUseCase {
    Organization registerOrganization(String name);
    Workspace createWorkspace(UUID orgId, String workspaceName);
    Organization getOrganization(UUID id);
}
