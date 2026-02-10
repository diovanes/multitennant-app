package com.diovanes.multitenant.controller;

import com.diovanes.multitenant.entity.Cliente;
import com.diovanes.multitenant.service.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Cliente API endpoints.
 * 
 * Provides REST endpoints to query cliente data for specific tenants.
 * All endpoints require a tenantId parameter to identify the tenant.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

    private final ClienteService clienteService;

    /**
     * Constructor with dependencies injection.
     *
     * @param clienteService the cliente service
     */
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * Get all clientes for a specific tenant.
     * 
     * Endpoint: GET /api/clientes/{tenantId}
     *
     * @param tenantId the tenant identifier
     * @return ResponseEntity containing a list of clientes in JSON format
     */
    @GetMapping("/{tenantId}")
    public ResponseEntity<Map<String, Object>> getAllClientes(@PathVariable String tenantId) {
        logger.info("REST: GET request to fetch all clientes for tenantId: {}", tenantId);
        
        try {
            // Fetch clientes from service
            List<Cliente> clientes = clienteService.getAllClientesByTenant(tenantId);
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tenantId", tenantId);
            response.put("total", clientes.size());
            response.put("data", clientes);
            
            logger.info("REST: Successfully returned {} clientes for tenantId: {}", clientes.size(), tenantId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("REST: Invalid tenant - {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid tenant identifier");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            
        } catch (Exception e) {
            logger.error("REST: Error fetching clientes for tenantId: {}", tenantId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", "An error occurred while processing your request");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get a specific cliente by id for a specific tenant.
     * 
     * Endpoint: GET /api/clientes/{tenantId}/{id}
     *
     * @param tenantId the tenant identifier
     * @param id       the cliente id
     * @return ResponseEntity containing the cliente in JSON format, or error if not found
     */
    @GetMapping("/{tenantId}/{id}")
    public ResponseEntity<Map<String, Object>> getClienteById(
            @PathVariable String tenantId,
            @PathVariable Long id) {
        
        logger.info("REST: GET request to fetch cliente with id: {} for tenantId: {}", id, tenantId);
        
        try {
            // Fetch cliente from service
            Cliente cliente = clienteService.getClienteByIdAndTenant(tenantId, id);
            
            if (cliente == null) {
                logger.warn("REST: Cliente with id: {} not found for tenantId: {}", id, tenantId);
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Cliente not found");
                errorResponse.put("tenantId", tenantId);
                errorResponse.put("id", id);
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tenantId", tenantId);
            response.put("data", cliente);
            
            logger.info("REST: Successfully returned cliente with id: {} for tenantId: {}", id, tenantId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("REST: Invalid parameter - {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid request parameter");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            
        } catch (Exception e) {
            logger.error("REST: Error fetching cliente with id: {} for tenantId: {}", id, tenantId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", "An error occurred while processing your request");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Health check endpoint.
     * 
     * Endpoint: GET /api/clientes/health
     *
     * @return ResponseEntity with health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        logger.info("REST: Health check endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Multitenant API is running");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
