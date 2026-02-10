package com.diovanes.multitenant.repository;

import com.diovanes.multitenant.entity.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository class for Cliente entity.
 * 
 * This repository is responsible for database operations related to Cliente records.
 * It uses JdbcTemplate for direct SQL execution and integrates with the
 * multitenant-datasource-hikari library to manage connections based on tenantId.
 */
@Repository
public class ClienteRepository {

    private static final Logger logger = LoggerFactory.getLogger(ClienteRepository.class);

    private final JdbcTemplate jdbcTemplate;
    private final MultitenantDataSourceManager multitenantDataSourceManager;

    private static final String SELECT_ALL_CLIENTES_SQL = "SELECT id, nome, email FROM clientes ORDER BY id";
    private static final String SELECT_CLIENTE_BY_ID_SQL = "SELECT id, nome, email FROM clientes WHERE id = ?";

    /**
     * Constructor with dependencies injection.
     *
     * @param jdbcTemplate                     the JdbcTemplate instance
     * @param multitenantDataSourceManager     the multitenant data source manager
     */
    public ClienteRepository(JdbcTemplate jdbcTemplate, 
                            MultitenantDataSourceManager multitenantDataSourceManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.multitenantDataSourceManager = multitenantDataSourceManager;
    }

    /**
     * Find all clientes for a specific tenant.
     *
     * @param tenantId the tenant identifier
     * @return a list of Cliente objects
     */
    public List<Cliente> findAllByTenant(String tenantId) {
        logger.debug("Fetching all clientes for tenantId: {}", tenantId);
        
        try {
            // Get the correct datasource for the tenant from the multitenant library
            var datasource = multitenantDataSourceManager.getDataSource(tenantId);
            var tenantJdbcTemplate = new JdbcTemplate(datasource);
            
            List<Cliente> clientes = tenantJdbcTemplate.query(
                    SELECT_ALL_CLIENTES_SQL,
                    clienteRowMapper()
            );
            
            logger.info("Successfully fetched {} clientes for tenantId: {}", clientes.size(), tenantId);
            return clientes;
            
        } catch (Exception e) {
            logger.error("Error fetching clientes for tenantId: {}", tenantId, e);
            throw new RuntimeException("Error fetching clientes for tenant: " + tenantId, e);
        }
    }

    /**
     * Find a cliente by id for a specific tenant.
     *
     * @param tenantId the tenant identifier
     * @param id       the cliente id
     * @return a Cliente object if found, null otherwise
     */
    public Cliente findByIdAndTenant(String tenantId, Long id) {
        logger.debug("Fetching cliente with id: {} for tenantId: {}", id, tenantId);
        
        try {
            // Get the correct datasource for the tenant from the multitenant library
            var datasource = multitenantDataSourceManager.getDataSource(tenantId);
            var tenantJdbcTemplate = new JdbcTemplate(datasource);
            
            List<Cliente> clientes = tenantJdbcTemplate.query(
                    SELECT_CLIENTE_BY_ID_SQL,
                    new Object[]{id},
                    clienteRowMapper()
            );
            
            if (!clientes.isEmpty()) {
                logger.info("Cliente with id: {} found for tenantId: {}", id, tenantId);
                return clientes.get(0);
            }
            
            logger.warn("Cliente with id: {} not found for tenantId: {}", id, tenantId);
            return null;
            
        } catch (Exception e) {
            logger.error("Error fetching cliente with id: {} for tenantId: {}", id, tenantId, e);
            throw new RuntimeException("Error fetching cliente for tenant: " + tenantId, e);
        }
    }

    /**
     * RowMapper implementation for Cliente entity.
     * Maps each row from the ResultSet to a Cliente object.
     *
     * @return a RowMapper for Cliente objects
     */
    private RowMapper<Cliente> clienteRowMapper() {
        return new RowMapper<Cliente>() {
            @Override
            public Cliente mapRow(ResultSet rs, int rowNum) throws SQLException {
                Cliente cliente = new Cliente();
                cliente.setId(rs.getLong("id"));
                cliente.setNome(rs.getString("nome"));
                cliente.setEmail(rs.getString("email"));
                return cliente;
            }
        };
    }
}
