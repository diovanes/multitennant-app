# Configuração da Biblioteca multitenant-datasource-hikari

Este documento fornece exemplos de como configurar a biblioteca `multitenant-datasource-hikari` para a aplicação Multitenant.

## Visão Geral

A biblioteca `multitenant-datasource-hikari` é responsável por gerenciar múltiplas conexões de banco de dados (DataSources), uma para cada tenant. Ela fornece um mecanismo para obter a DataSource correta baseada no `tenantId`.

## Instalação

### Adicionar ao pom.xml

A dependência já está configurada no `pom.xml`:

```xml
<dependency>
    <groupId>com.diovanes</groupId>
    <artifactId>multitenant-datasource-hikari</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Configuração

### Opção 1: Através de application.properties

Adicione no arquivo `src/main/resources/application.properties`:

```properties
# =====================================================
# Multitenant Datasource Configuration
# =====================================================

# Tenant 1 Configuration
multitenant.datasources.tenant-001.url=jdbc:postgresql://localhost:5432/tenant_001_db
multitenant.datasources.tenant-001.username=postgres
multitenant.datasources.tenant-001.password=postgres
multitenant.datasources.tenant-001.driverClassName=org.postgresql.Driver
multitenant.datasources.tenant-001.maximumPoolSize=5
multitenant.datasources.tenant-001.minimumIdle=1
multitenant.datasources.tenant-001.connectionTimeout=30000

# Tenant 2 Configuration
multitenant.datasources.tenant-002.url=jdbc:postgresql://localhost:5432/tenant_002_db
multitenant.datasources.tenant-002.username=postgres
multitenant.datasources.tenant-002.password=postgres
multitenant.datasources.tenant-002.driverClassName=org.postgresql.Driver
multitenant.datasources.tenant-002.maximumPoolSize=5
multitenant.datasources.tenant-002.minimumIdle=1
multitenant.datasources.tenant-002.connectionTimeout=30000

# Tenant 3 Configuration
multitenant.datasources.tenant-003.url=jdbc:postgresql://localhost:5432/tenant_003_db
multitenant.datasources.tenant-003.username=postgres
multitenant.datasources.tenant-003.password=postgres
multitenant.datasources.tenant-003.driverClassName=org.postgresql.Driver
multitenant.datasources.tenant-003.maximumPoolSize=5
multitenant.datasources.tenant-003.minimumIdle=1
multitenant.datasources.tenant-003.connectionTimeout=30000
```

### Opção 2: Através de um arquivo YAML

Crie `src/main/resources/application-multitenant.yml`:

```yaml
multitenant:
  datasources:
    tenant-001:
      url: jdbc:postgresql://localhost:5432/tenant_001_db
      username: postgres
      password: postgres
      driverClassName: org.postgresql.Driver
      maximumPoolSize: 5
      minimumIdle: 1
      connectionTimeout: 30000
    
    tenant-002:
      url: jdbc:postgresql://localhost:5432/tenant_002_db
      username: postgres
      password: postgres
      driverClassName: org.postgresql.Driver
      maximumPoolSize: 5
      minimumIdle: 1
      connectionTimeout: 30000
    
    tenant-003:
      url: jdbc:postgresql://localhost:5432/tenant_003_db
      username: postgres
      password: postgres
      driverClassName: org.postgresql.Driver
      maximumPoolSize: 5
      minimumIdle: 1
      connectionTimeout: 30000
```

### Opção 3: Através de Configuração Java

Crie uma classe `@Configuration` para configurar os tenants programaticamente:

```java
package com.diovanes.multitenant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import com.diovanes.multitenant.datasource.MultitenantDataSourceProvider;
import java.util.Map;

@Configuration
public class MultitenantDataSourceConfig {

    public void configureTenants(Map<String, TenantDataSourceProperties> tenants) {
        // Registrar cada tenant na biblioteca
        for (Map.Entry<String, TenantDataSourceProperties> entry : tenants.entrySet()) {
            String tenantId = entry.getKey();
            TenantDataSourceProperties props = entry.getValue();
            
            // Chamar a API da biblioteca para registrar o tenant
            MultitenantDataSourceProvider.registerDataSource(
                tenantId,
                props.getUrl(),
                props.getUsername(),
                props.getPassword(),
                props.getMaximumPoolSize(),
                props.getMinimumIdle()
            );
        }
    }
}

@ConfigurationProperties(prefix = "multitenant.datasources")
class TenantDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private Integer maximumPoolSize;
    private Integer minimumIdle;
    
    // Getters e Setters...
}
```

## Exemplo de Setup Completo do Banco de Dados

Para cada tenant, você precisa:

### Tenant 001

```sql
-- Criar banco de dados para tenant 001
CREATE DATABASE tenant_001_db
    WITH ENCODING 'UTF8'
    TEMPLATE = template0;

-- Conectar ao banco
\c tenant_001_db

-- Criar tabela de clientes
CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inserir dados de exemplo
INSERT INTO clientes (nome, email) VALUES 
('João Silva', 'joao@tenant001.com'),
('Maria Santos', 'maria@tenant001.com'),
('Pedro Oliveira', 'pedro@tenant001.com');
```

### Tenant 002

```sql
-- Criar banco de dados para tenant 002
CREATE DATABASE tenant_002_db
    WITH ENCODING 'UTF8'
    TEMPLATE = template0;

-- Conectar ao banco
\c tenant_002_db

-- Criar tabela de clientes
CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inserir dados de exemplo
INSERT INTO clientes (nome, email) VALUES 
('Alice Costa', 'alice@tenant002.com'),
('Bob Martinez', 'bob@tenant002.com'),
('Carol Davis', 'carol@tenant002.com');
```

## Estrutura de Requisição com Múltiplos Tenants

```
Cliente HTTP Request
    ↓
Controller recebe tenantId
    ↓
Service valida tenantId
    ↓
MultitenantDataSourceManager.getDataSource(tenantId)
    ↓
MultitenantDataSourceProvider.getDataSource(tenantId) [API da biblioteca]
    ↓
Retorna DataSource específico do tenant
    ↓
Repository cria JdbcTemplate com DataSource do tenant
    ↓
Executa query na tabela clientes do BD do tenant
    ↓
Retorna dados apenas do tenant solicitado
```

## Teste dos Múltiplos Tenants

### Listar clientes do Tenant 001

```bash
curl -X GET http://localhost:8080/api/clientes/tenant-001
```

**Resposta esperada:**
```json
{
  "success": true,
  "tenantId": "tenant-001",
  "total": 3,
  "data": [
    {"id": 1, "nome": "João Silva", "email": "joao@tenant001.com"},
    {"id": 2, "nome": "Maria Santos", "email": "maria@tenant001.com"},
    {"id": 3, "nome": "Pedro Oliveira", "email": "pedro@tenant001.com"}
  ]
}
```

### Listar clientes do Tenant 002

```bash
curl -X GET http://localhost:8080/api/clientes/tenant-002
```

**Resposta esperada:**
```json
{
  "success": true,
  "tenantId": "tenant-002",
  "total": 3,
  "data": [
    {"id": 1, "nome": "Alice Costa", "email": "alice@tenant002.com"},
    {"id": 2, "nome": "Bob Martinez", "email": "bob@tenant002.com"},
    {"id": 3, "nome": "Carol Davis", "email": "carol@tenant002.com"}
  ]
}
```

## Fluxo de Isolamento de Dados

A biblioteca garante isolamento de dados entre tenants:

```
Tenant 001 Database:
  clientes table
  ├── id: 1, nome: João Silva, email: joao@tenant001.com
  ├── id: 2, nome: Maria Santos, email: maria@tenant001.com
  └── id: 3, nome: Pedro Oliveira, email: pedro@tenant001.com

Tenant 002 Database:
  clientes table
  ├── id: 1, nome: Alice Costa, email: alice@tenant002.com
  ├── id: 2, nome: Bob Martinez, email: bob@tenant002.com
  └── id: 3, nome: Carol Davis, email: carol@tenant002.com

REQUEST: GET /api/clientes/tenant-001
  → Usa DataSource de tenant-001
  → Acessa apenas tenant_001_db
  → Retorna clientes de tenant-001
```

## Configuração de Ambiente

### Para Desenvolvimento Local

```properties
# application-dev.properties
multitenant.datasources.tenant-001.url=jdbc:postgresql://localhost:5432/tenant_001_db
multitenant.datasources.tenant-002.url=jdbc:postgresql://localhost:5432/tenant_002_db
multitenant.datasources.tenant-003.url=jdbc:postgresql://localhost:5432/tenant_003_db
```

### Para Teste

```properties
# application-test.properties
multitenant.datasources.tenant-001.url=jdbc:postgresql://localhost:5432/tenant_001_test_db
multitenant.datasources.tenant-002.url=jdbc:postgresql://localhost:5432/tenant_002_test_db
```

### Para Produção

```properties
# application-prod.properties
multitenant.datasources.tenant-001.url=jdbc:postgresql://prod-db-server:5432/tenant_001_db
multitenant.datasources.tenant-001.username=${DB_USER}
multitenant.datasources.tenant-001.password=${DB_PASSWORD}

multitenant.datasources.tenant-002.url=jdbc:postgresql://prod-db-server:5432/tenant_002_db
multitenant.datasources.tenant-002.username=${DB_USER}
multitenant.datasources.tenant-002.password=${DB_PASSWORD}
```

## Recursos Adicionais

- **Documentação Oficial:** https://github.com/diovanes/multitenant-datasource-hikari
- **HikariCP Settings:** https://github.com/brettwooldridge/HikariCP
- **PostgreSQL Connection Strings:** https://www.postgresql.org/docs/current/libpq-connect.html

## Troubleshooting

### Erro: "DataSource not found for tenantId"

**Causa:** Tenant não foi configurado na biblioteca

**Solução:**
1. Verifique se o tenantId está configurado no application.properties
2. Verifique se o nome do banco de dados está correto
3. Certifique-se de que o banco de dados existe no PostgreSQL

### Erro: "Cannot get a connection"

**Causa:** DataSource existe mas não consegue conectar ao banco

**Solução:**
1. Verifique se PostgreSQL está rodando
2. Verifique as credenciais (username/password)
3. Verifique se o servidor é acessível na porta 5432
4. Verifique se o banco de dados existe

### Erro: "Connection pool is full"

**Causa:** Muitas conexões ativas, pool de conexões esgotou

**Solução:**
1. Aumentar `maximumPoolSize` na configuração
2. Verificar se há conexões não fechadas
3. Aumentar timeout de conexão

## Melhorias Futuras

1. **Carregamento Dinâmico:** Registrar tenants em tempo de execução
2. **Cache:** Cache de DataSources para melhor performance
3. **Monitoramento:** Métricas de pool de conexões por tenant
4. **Fallback:** Tenant padrão se tenantId não for reconhecido
5. **Replicação:** Suporte para múltiplos bancos de dados por tenant

---

**Última atualização:** 9 de fevereiro de 2026
