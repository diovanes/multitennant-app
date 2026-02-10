# Atualização da Implementação Multi-Tenant

## Resumo das Alterações

Esta atualização integra a versão mais recente da biblioteca `multitenant-datasource-hikari` com as seguintes melhorias:

### 1. **Novas Dependências Adicionadas**
   - `snakeyaml 2.2` - Parsing de configurações YAML
   - `caffeine 3.1.8` - Cache inteligente com expiração automática

### 2. **Arquivo de Configuração de Tenants**
   - **Local**: `src/main/resources/tenants.yml`
   - **Propósito**: Centraliza a configuração de todos os tenants
   - **Características**:
     - Carregamento automático no startup
     - Suporte a múltiplos tenants
     - Configuração individual de host, porta, credenciais e pool size
     - Cache inteligente com TTL de 2 horas

### 3. **Spring Configuration Bean**
   - **Arquivo**: `src/main/java/com/diovanes/multitenant/config/DataSourceManagerConfig.java`
   - **Propósito**: Inicializa o `DataSourceManager` como bean Spring
   - **Benefícios**:
     - Instância única (singleton) através da aplicação
     - Gerenciamento automático do ciclo de vida pelo Spring
     - Injeção de dependência simplificada

### 4. **MultitenantDataSourceManager Refatorado**
   - **Alterações**:
     - Agora usa injeção de dependência do `DataSourceManager`
     - Não cria novas instâncias a cada chamada (problema anterior corrigido)
     - Fornece métodos para:
       - `getDataSource(tenantId)` - Retorna HikariDataSource cacheada
       - `getConnection(tenantId)` - Retorna Connection direta
       - `isTenantValid(tenantId)` - Valida disponibilidade do tenant
       - `invalidateDataSourceCache(tenantId)` - Força recreação do pool
       - `closeAll()` - Fecha todos os pools (chamado no shutdown)

### 5. **Startup/Shutdown Melhorado**
   - **MultitennantAppApplication.java**:
     - Shutdown hook para fechar datasources gracefully
     - Logs estruturados durante inicialização e parada

## Fluxo de Funcionamento

```
Aplicação inicia
    ↓
DataSourceManagerConfig inicializa DataSourceManager
    ↓
DataSourceManager carrega tenants.yml do classpath
    ↓
Todos os tenants são pré-carregados e cacheados
    ↓
Requisições chegam → MultitenantDataSourceManager.getDataSource(tenantId)
    ↓
Retorna HikariDataSource cacheada (reutiliza conexões)
    ↓
ClienteRepository usa JdbcTemplate com datasource tenant-específica
    ↓
Aplicação fecha → closeAll() fecha todos os pools
```

## Cache Behavior

- **Preload Inicial**: Todos os tenants são carregados em memória na inicialização
- **Reutilização**: Datasources são cacheadas e reutilizadas via Caffeine
- **Expiração**: A cada 2 horas, o cache é revalidado (TTL = 120 minutos)
- **Lazy Loading**: Novos tenants não pré-configurados são carregados sob demanda
- **Thread-Safe**: Operações de cache são sincronizadas para multi-threading

## Configuração de Tenants

### Exemplo (tenants.yml):

```yaml
tenants:
  tenant1:
    host: localhost
    port: 5432
    user: myuser
    password: mypass
    database: mydb
    schema: public
    poolSize: 10
    connectionTimeoutMs: 30000

  tenant2:
    host: db2.example.com
    port: 5432
    user: user2
    password: pass2
    database: tenant2db
    schema: public
    poolSize: 5
    connectionTimeoutMs: 30000
```

### Campos Suportados:

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|------------|-----------|
| host | String | Sim | Host do banco de dados |
| port | Integer | Sim | Porta do banco de dados |
| user | String | Sim | Usuário de conexão |
| password | String | Sim | Senha de conexão |
| database | String | Sim | Nome do banco de dados |
| schema | String | Não | Schema padrão (padrão: public) |
| poolSize | Integer | Não | Tamanho do pool de conexões (padrão: 10) |
| connectionTimeoutMs | Long | Não | Timeout de conexão em ms (padrão: 30000) |

## Uso na Aplicação

### ClienteRepository (Exemplo):

```java
@Repository
public class ClienteRepository {
    
    private final JdbcTemplate jdbcTemplate;
    private final MultitenantDataSourceManager multitenantDataSourceManager;
    
    public List<Cliente> findAllByTenant(String tenantId) {
        // Obter datasource do tenant
        var datasource = multitenantDataSourceManager.getDataSource(tenantId);
        var tenantJdbcTemplate = new JdbcTemplate(datasource);
        
        // Executar query
        List<Cliente> clientes = tenantJdbcTemplate.query(
            "SELECT id, nome, email FROM clientes ORDER BY id",
            clienteRowMapper()
        );
        
        return clientes;
    }
}
```

## Melhorias em Relação à Versão Anterior

| Aspecto | Antes | Depois |
|--------|-------|--------|
| Gerenciamento de Pools | Manual por tenant | Automático via biblioteca |
| Cache | Não havia cache eficiente | Caffeine com TTL 2h |
| Inicialização | Lazy/sob-demanda | Preload no startup |
| Lifecycle | Manual | Gerenciado pelo Spring |
| Configuração | Hardcoded | Arquivo YAML centralizado |
| Thread-Safety | Potencialmente inseguro | Garantido pela biblioteca |
| Performance | Múltiplas pools desnecessárias | Reutilização inteligente de pools |

## Endpoints da API

Todos os endpoints usam agora a nova implementação:

### GET /api/clientes/{tenantId}
Retorna todos os clientes de um tenant

```bash
curl http://localhost:8080/api/clientes/tenant1
```

### GET /api/clientes/{tenantId}/{id}
Retorna um cliente específico

```bash
curl http://localhost:8080/api/clientes/tenant1/123
```

## Próximos Passos Recomendados

1. **Atualizar tenants.yml** com credenciais reais
2. **Criar databases** para cada tenant
3. **Criar tables** clientes em cada database
4. **Adicionar dados** de teste
5. **Testar endpoints** para validar funcionamento
6. **Monitorar logs** para verificar comportamento do cache

## Troubleshooting

### Erro: "No configuration found for tenant"
- Verifique se o tenantId existe em tenants.yml
- Verifique se o arquivo está em src/main/resources/

### Erro: "Connection refused"
- Verifique conectividade com o banco de dados
- Verifique host, porta e credenciais em tenants.yml

### Performance lenta na primeira chamada
- Normal: primeira chamada carrega o pool de conexões
- Chamadas subsequentes serão rápidas (cacheadas)

## Versão da Biblioteca

- **multitenant-datasource-hikari**: 0.1.0
- **HikariCP**: 5.1.0
- **PostgreSQL Driver**: 42.7.1
- **Caffeine**: 3.1.8
- **SnakeYAML**: 2.2

