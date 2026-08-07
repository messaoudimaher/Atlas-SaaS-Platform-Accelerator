package com.atlas.org.config;

import com.atlas.org.domain.port.in.OrganizationUseCase;
import com.atlas.org.domain.port.out.OrganizationRepositoryPort;
import com.atlas.org.domain.service.OrganizationDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppBeanConfig {

    @Bean
    public OrganizationUseCase organizationUseCase(OrganizationRepositoryPort repositoryPort) {
        return new OrganizationDomainService(repositoryPort);
    }
}
