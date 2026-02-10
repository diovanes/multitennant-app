package com.diovanes.multitenant;

import com.diovanes.multitenant.repository.MultitenantDataSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

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
 * - Automatic connection pool management with Caffeine cache
 *
 * Usage:
 * 1. Ensure PostgreSQL is running on localhost:5432
 * 2. Create databases for each tenant defined in tenants.yml
 * 3. Create the 'clientes' table with columns: id (BIGINT), nome (VARCHAR), email (VARCHAR)
 * 4. Configure tenants in src/main/resources/tenants.yml
 * 5. Run the application: mvn spring-boot:run
 * 
 * Endpoints:
 * - GET /api/clientes/health - Health check
 * - GET /api/clientes/{tenantId} - Get all clientes for a tenant
 * - GET /api/clientes/{tenantId}/{id} - Get a specific cliente by id
 *
 * Tenant Configuration:
 * The application loads tenant configurations from tenants.yml at startup.
 * Each tenant can have its own database host, port, credentials, and connection pool size.
 */
@SpringBootApplication
public class MultitennantAppApplication {

    private static final Logger logger = LoggerFactory.getLogger(MultitennantAppApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Multitenant Application...");
        ConfigurableApplicationContext context = SpringApplication.run(MultitennantAppApplication.class, args);
        logger.info("Multitenant Application started successfully!");

        // Register shutdown hook to properly close datasources
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application shutdown initiated, closing datasources...");
            try {
                MultitenantDataSourceManager manager = context.getBean(MultitenantDataSourceManager.class);
                manager.closeAll();
                logger.info("All datasources closed successfully");
            } catch (Exception e) {
                logger.error("Error closing datasources during shutdown", e);
            }
        }));
    }
}
