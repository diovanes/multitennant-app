package com.diovanes.multitenant.config;

import com.diovanes.datasource.multitenant.DataSourceManager;
import com.diovanes.datasource.multitenant.cache.DataSourceCacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for initializing the DataSourceManager bean.
 *
 * This configuration creates a singleton DataSourceManager instance that is used
 * throughout the application to manage tenant database connections.
 */
@Configuration
public class DataSourceManagerConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceManagerConfig.class);

    /**
     * Creates and initializes the DataSourceManager bean.
     *
     * Uses the tenants.yml file from classpath with default cache configuration
     * (2 hours TTL, 100 max size).
     *
     * @return DataSourceManager instance
     * @throws Exception if the configuration file cannot be loaded
     */
    @Bean
    public DataSourceManager dataSourceManager() throws Exception {
        logger.info("Initializing DataSourceManager with tenants.yml from classpath");

        // Load tenants configuration from classpath
        var cacheConfig = DataSourceCacheConfig.defaults();
        var manager = new DataSourceManager("tenants.yml", true, cacheConfig);

        logger.info("DataSourceManager initialized successfully");
        return manager;
    }
}

