package com.atlas.shared.security.tenant;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class TenantConnectionProvider implements MultiTenantConnectionProvider {

    private static final Logger log = LoggerFactory.getLogger(TenantConnectionProvider.class);
    private static final String DEFAULT_TENANT = "public";
    private final DataSource dataSource;

    public TenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        log.debug("Retrieving database connection for tenant: {}", tenantIdentifier);
        final Connection connection = getAnyConnection();
        try (Statement statement = connection.createStatement()) {
            String tenantSchema = (tenantIdentifier != null && !DEFAULT_TENANT.equals(tenantIdentifier)) 
                    ? "tenant_" + tenantIdentifier.toString().replace("-", "_") 
                    : DEFAULT_TENANT;
            
            log.trace("Executing database schema switch: SET search_path TO {}", tenantSchema);
            statement.execute("SET search_path TO " + tenantSchema);
        } catch (SQLException e) {
            log.error("Failed to alter database connection search path to tenant schema: {}", tenantIdentifier, e);
            connection.close();
            throw e;
        }
        return connection;
    }

    @Override
    public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
        log.debug("Releasing database connection for tenant: {}", tenantIdentifier);
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO " + DEFAULT_TENANT);
        } catch (SQLException e) {
            log.error("Failed to reset connection search path back to public: {}", tenantIdentifier, e);
        } finally {
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return true;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}
