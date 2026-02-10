package com.diovanes.multitenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Multitenant Spring Boot Application.
 * 
 * This application provides REST endpoints to query cliente data from a PostgreSQL database
 * in a multi-tenant environment. It uses the multitenant-datasource-hikari library
 * to manage database connections for different tenants.
 * 
 * Application Features:
 * - Multi-tenant support with tenant-based data isolation
 * - REST API endpoints for cliente data queries
 * - JdbcTemplate for direct SQL execution
 * - PostgreSQL database integration
 * - Structured logging with SLF4J
 * - Layered architecture (Controller → Service → Repository)
 * 
 * Usage:
 * 1. Ensure PostgreSQL is running on localhost:5432
 * 2. Create a database named 'multitenant_db'
 * 3. Create the 'clientes' table with columns: id (BIGINT), nome (VARCHAR), email (VARCHAR)
 * 4. Configure multitenant-datasource-hikari with tenant data sources
 * 5. Run the application: mvn spring-boot:run
 * 
 * Endpoints:
 * - GET /api/clientes/health - Health check
 * - GET /api/clientes/{tenantId} - Get all clientes for a tenant
 * - GET /api/clientes/{tenantId}/{id} - Get a specific cliente by id
 */
@SpringBootApplication
public class MultitennantAppApplication {

    private static final Logger logger = LoggerFactory.getLogger(MultitennantAppApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Multitenant Application...");
        SpringApplication.run(MultitennantAppApplication.class, args);
        logger.info("Multitenant Application started successfully!");
    }
}
