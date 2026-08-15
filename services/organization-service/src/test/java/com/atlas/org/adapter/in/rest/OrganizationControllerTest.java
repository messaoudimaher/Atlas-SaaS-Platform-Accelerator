package com.atlas.org.adapter.in.rest;

import com.atlas.org.adapter.out.metrics.OrgMetricsService;
import com.atlas.org.domain.model.Organization;
import com.atlas.org.domain.model.Workspace;
import com.atlas.org.domain.port.in.OrganizationUseCase;
import com.atlas.shared.kernel.model.ApiResponse;
import com.atlas.shared.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrganizationControllerTest {

    private OrganizationUseCase organizationUseCase;
    private OrgMetricsService metricsService;
    private OrganizationController controller;

    @BeforeEach
    void setUp() {
        organizationUseCase = mock(OrganizationUseCase.class);
        metricsService = mock(OrgMetricsService.class);
        controller = new OrganizationController(organizationUseCase, metricsService);
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void registerOrganization_returnsCreatedOrganization() {
        Organization mockOrg = Organization.createNew("Test Org");
        when(organizationUseCase.registerOrganization("Test Org")).thenReturn(mockOrg);

        OrganizationController.RegisterOrganizationRequest request = 
                new OrganizationController.RegisterOrganizationRequest("Test Org");

        ResponseEntity<ApiResponse<Organization>> response = controller.registerOrganization(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
        assertEquals(mockOrg.getId(), response.getBody().data().getId());
        verify(metricsService, times(1)).incrementOrgCreated();
    }

    @Test
    void createWorkspace_returnsCreatedWorkspace_whenTenantContextPresent() {
        UUID orgId = UUID.randomUUID();
        TenantContext.setCurrentTenant(orgId.toString());

        Workspace mockWorkspace = Workspace.createNew(orgId, "Test Workspace");
        when(organizationUseCase.createWorkspace(orgId, "Test Workspace")).thenReturn(mockWorkspace);

        OrganizationController.CreateWorkspaceRequest request = 
                new OrganizationController.CreateWorkspaceRequest("Test Workspace");

        ResponseEntity<ApiResponse<Workspace>> response = controller.createWorkspace(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
        assertEquals(mockWorkspace.getId(), response.getBody().data().getId());
        verify(metricsService, times(1)).incrementWorkspaceCreated();
    }

    @Test
    void createWorkspace_returnsBadRequest_whenTenantContextMissing() {
        TenantContext.clear();

        OrganizationController.CreateWorkspaceRequest request = 
                new OrganizationController.CreateWorkspaceRequest("Test Workspace");

        ResponseEntity<ApiResponse<Workspace>> response = controller.createWorkspace(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertNull(response.getBody().data());
        verify(metricsService, never()).incrementWorkspaceCreated();
    }
}
