package com.diovanes.multitenant.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * MultitenantDataSourceManager is responsible for managing database connections
 * for different tenants using the multitenant-datasource-hikari library.
 * 
 * This class acts as a bridge between the application and the multitenant-datasource-hikari
 * library, providing the correct DataSource for each tenant.
 */
@Component
public class MultitenantDataSourceManager {

    private static final Logger logger = LoggerFactory.getLogger(MultitenantDataSourceManager.class);

    /**
     * Get the datasource for a specific tenant.
     * 
     * This method calls the multitenant-datasource-hikari library API to retrieve
     * the correct database connection for the given tenantId.
     *
     * @param tenantId the tenant identifier
     * @return the DataSource for the tenant
     * @throws RuntimeException if tenant is not found or connection fails
     */
    public DataSource getDataSource(String tenantId) {
        logger.debug("Getting datasource for tenantId: {}", tenantId);
        
        if (tenantId == null || tenantId.trim().isEmpty()) {
            logger.error("tenantId cannot be null or empty");
            throw new IllegalArgumentException("tenantId cannot be null or empty");
        }
        
        try {
            // Call the multitenant-datasource-hikari library API
            // The library provides a way to retrieve the correct DataSource based on tenantId
            DataSource dataSource = com.example.datasource.multitenant.DataSourceManager.getDataSource(tenantId);
            
            if (dataSource == null) {
                logger.error("DataSource not found for tenantId: {}", tenantId);
                throw new RuntimeException("DataSource not found for tenantId: " + tenantId);
            }
            
            logger.debug("DataSource retrieved successfully for tenantId: {}", tenantId);
            return dataSource;
            
        } catch (Exception e) {
            logger.error("Error retrieving datasource for tenantId: {}", tenantId, e);
            throw new RuntimeException("Error retrieving datasource for tenantId: " + tenantId, e);
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
            return dataSource != null && dataSource.getConnection() != null;
        } catch (Exception e) {
            logger.warn("Tenant validation failed for tenantId: {}", tenantId, e);
            return false;
        }
    }
}
