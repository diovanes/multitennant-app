package com.diovanes.multitenant.repository;

import com.diovanes.datasource.multitenant.DataSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * MultitenantDataSourceManager is responsible for managing database connections
 * for different tenants using the multitenant-datasource-hikari library.
 * 
 * This class acts as a wrapper/adapter between the application and the multitenant-datasource-hikari
 * library, providing the correct DataSource for each tenant.
 *
 * The actual DataSourceManager is injected via Spring configuration.
 */
@Component
public class MultitenantDataSourceManager {

    private static final Logger logger = LoggerFactory.getLogger(MultitenantDataSourceManager.class);

    private final DataSourceManager dataSourceManager;

    /**
     * Constructor with dependency injection.
     *
     * @param dataSourceManager the DataSourceManager instance from Spring config
     */
    public MultitenantDataSourceManager(DataSourceManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
    }

    /**
     * Get the HikariDataSource for a specific tenant.
     *
     * This method calls the multitenant-datasource-hikari library API to retrieve
     * the correct HikariDataSource for the given tenantId.
     * The datasource is cached automatically by the library.
     *
     * @param tenantId the tenant identifier
     * @return the HikariDataSource for the tenant
     * @throws RuntimeException if tenant is not found or datasource retrieval fails
     */
    public DataSource getDataSource(String tenantId) {
        logger.debug("Getting datasource for tenantId: {}", tenantId);

        if (tenantId == null || tenantId.trim().isEmpty()) {
            logger.error("tenantId cannot be null or empty");
            throw new IllegalArgumentException("tenantId cannot be null or empty");
        }

        try {
            // Get the HikariDataSource from the library
            // The datasource is automatically cached and reused
            HikariDataSource dataSource = dataSourceManager.getDataSource(tenantId);

            logger.debug("DataSource retrieved successfully for tenantId: {}", tenantId);
            return dataSource;
        } catch (SQLException e) {
            logger.error("SQL error retrieving datasource for tenantId: {}", tenantId, e);
            throw new RuntimeException("Error retrieving datasource for tenantId: " + tenantId, e);
        } catch (Exception e) {
            logger.error("Error retrieving datasource for tenantId: {}", tenantId, e);
            throw new RuntimeException("Error retrieving datasource for tenantId: " + tenantId, e);
        }
    }

    /**
     * Get a direct Connection for a specific tenant.
     *
     * Use this method when you need a direct connection instead of a datasource.
     * Remember to close the connection after use.
     *
     * @param tenantId the tenant identifier
     * @return a Connection for the tenant
     * @throws RuntimeException if tenant is not found or connection retrieval fails
     */
    public Connection getConnection(String tenantId) {
        logger.debug("Getting connection for tenantId: {}", tenantId);

        if (tenantId == null || tenantId.trim().isEmpty()) {
            logger.error("tenantId cannot be null or empty");
            throw new IllegalArgumentException("tenantId cannot be null or empty");
        }

        try {
            Connection connection = dataSourceManager.getConnection(tenantId);
            logger.debug("Connection retrieved successfully for tenantId: {}", tenantId);
            return connection;
        } catch (SQLException e) {
            logger.error("SQL error retrieving connection for tenantId: {}", tenantId, e);
            throw new RuntimeException("Error retrieving connection for tenantId: " + tenantId, e);
        } catch (Exception e) {
            logger.error("Error retrieving connection for tenantId: {}", tenantId, e);
            throw new RuntimeException("Error retrieving connection for tenantId: " + tenantId, e);
        }
    }

    /**
     * Validates if a tenant exists and has a valid datasource.
     *
     * @param tenantId the tenant identifier
     * @return true if tenant is valid and datasource is available, false otherwise
     */
    public boolean isTenantValid(String tenantId) {
        logger.debug("Validating tenant: {}", tenantId);
        
        try {
            DataSource dataSource = getDataSource(tenantId);
            var connection = dataSource.getConnection();
            connection.close();
            return true;
        } catch (Exception e) {
            logger.warn("Tenant validation failed for tenantId: {}", tenantId, e);
            return false;
        }
    }

    /**
     * Close all datasources and clear the cache.
     * This should be called on application shutdown.
     */
    public void closeAll() {
        logger.info("Closing all datasources and clearing cache");
        try {
            dataSourceManager.closeAll();
            logger.info("All datasources closed successfully");
        } catch (Exception e) {
            logger.error("Error closing datasources", e);
        }
    }

    /**
     * Invalidate a specific datasource from the cache.
     * This forces the library to create a new connection pool for the next request.
     *
     * @param tenantId the tenant identifier
     */
    public void invalidateDataSourceCache(String tenantId) {
        logger.info("Invalidating datasource cache for tenantId: {}", tenantId);
        try {
            dataSourceManager.invalidateDataSourceCache(tenantId);
            logger.info("Datasource cache invalidated for tenantId: {}", tenantId);
        } catch (Exception e) {
            logger.error("Error invalidating datasource cache for tenantId: {}", tenantId, e);
        }
    }
}
