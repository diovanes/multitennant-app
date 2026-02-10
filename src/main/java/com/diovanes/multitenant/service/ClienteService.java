package com.diovanes.multitenant.service;

import com.diovanes.multitenant.entity.Cliente;
import com.diovanes.multitenant.repository.ClienteRepository;
import com.diovanes.multitenant.repository.MultitenantDataSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for handling Cliente business logic.
 * 
 * This service orchestrates the business logic for Cliente operations,
 * delegating data access to the ClienteRepository and validating tenant access
 * through the MultitenantDataSourceManager.
 */
@Service
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteRepository clienteRepository;
    private final MultitenantDataSourceManager multitenantDataSourceManager;

    /**
     * Constructor with dependencies injection.
     *
     * @param clienteRepository                the cliente repository
     * @param multitenantDataSourceManager     the multitenant data source manager
     */
    public ClienteService(ClienteRepository clienteRepository,
                         MultitenantDataSourceManager multitenantDataSourceManager) {
        this.clienteRepository = clienteRepository;
        this.multitenantDataSourceManager = multitenantDataSourceManager;
    }

    /**
     * Retrieve all clientes for a specific tenant.
     * 
     * This method validates that the tenant exists before attempting to fetch clientes.
     *
     * @param tenantId the tenant identifier
     * @return a list of Cliente objects
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws RuntimeException if an error occurs while fetching clientes
     */
    public List<Cliente> getAllClientesByTenant(String tenantId) {
        logger.info("Service: Fetching all clientes for tenantId: {}", tenantId);
        
        // Validate tenant
        if (!isValidTenant(tenantId)) {
            logger.error("Invalid tenant: {}", tenantId);
            throw new IllegalArgumentException("Invalid tenantId: " + tenantId);
        }

        // Fetch clientes from repository
        List<Cliente> clientes = clienteRepository.findAllByTenant(tenantId);
        logger.info("Service: Successfully fetched {} clientes for tenantId: {}", clientes.size(), tenantId);
        
        return clientes;
    }

    /**
     * Retrieve a specific cliente by id for a specific tenant.
     *
     * @param tenantId the tenant identifier
     * @param id       the cliente id
     * @return the Cliente object if found, null otherwise
     * @throws IllegalArgumentException if tenantId is invalid
     * @throws RuntimeException if an error occurs while fetching the cliente
     */
    public Cliente getClienteByIdAndTenant(String tenantId, Long id) {
        logger.info("Service: Fetching cliente with id: {} for tenantId: {}", id, tenantId);
        
        // Validate tenant
        if (!isValidTenant(tenantId)) {
            logger.error("Invalid tenant: {}", tenantId);
            throw new IllegalArgumentException("Invalid tenantId: " + tenantId);
        }

        // Validate id parameter
        if (id == null || id <= 0) {
            logger.error("Invalid cliente id: {}", id);
            throw new IllegalArgumentException("Invalid cliente id: " + id);
        }

        // Fetch cliente from repository
        Cliente cliente = clienteRepository.findByIdAndTenant(tenantId, id);
        
        if (cliente != null) {
            logger.info("Service: Cliente with id: {} found for tenantId: {}", id, tenantId);
        } else {
            logger.info("Service: Cliente with id: {} not found for tenantId: {}", id, tenantId);
        }
        
        return cliente;
    }

    /**
     * Validate if a tenant is valid and has access to the database.
     *
     * @param tenantId the tenant identifier
     * @return true if tenant is valid, false otherwise
     */
    private boolean isValidTenant(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            logger.warn("Attempted to validate null or empty tenantId");
            return false;
        }

        try {
            return multitenantDataSourceManager.isTenantValid(tenantId);
        } catch (Exception e) {
            logger.error("Error validating tenant: {}", tenantId, e);
            return false;
        }
    }
}
