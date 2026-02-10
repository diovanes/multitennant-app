# Guia de Uso - Aplicação Multi-Tenant

## Inicialização da Aplicação

### 1. Pré-requisitos

- Java 17+
- Maven 3.9.0+
- PostgreSQL 12+
- Git

### 2. Preparação do Ambiente

#### A. Clonar o repositório

```bash
git clone https://github.com/diovanes/multitennant-app.git
cd multitennant-app
```

#### B. Instalar dependências

```bash
mvn clean install
```

#### C. Configurar Tenants (tenants.yml)

Edite `src/main/resources/tenants.yml` com suas credenciais:

```yaml
tenants:
  tenant1:
    host: localhost
    port: 5432
    user: postgres
    password: seu_password
    database: tenant1_db
    schema: public
    poolSize: 10
    connectionTimeoutMs: 30000

  tenant2:
    host: localhost
    port: 5432
    user: postgres
    password: seu_password
    database: tenant2_db
    schema: public
    poolSize: 10
    connectionTimeoutMs: 30000
```

#### D. Criar Databases e Tabelas

```bash
# Conectar ao PostgreSQL
psql -U postgres

# Criar databases
CREATE DATABASE tenant1_db;
CREATE DATABASE tenant2_db;

# Sair
\q

# Criar tabelas em cada database
psql -U postgres -d tenant1_db -c "
CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);
"

psql -U postgres -d tenant2_db -c "
CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);
"
```

#### E. Inserir Dados de Teste

```bash
# Tenant 1
psql -U postgres -d tenant1_db -c "
INSERT INTO clientes (nome, email) VALUES
('João Silva', 'joao@example.com'),
('Maria Santos', 'maria@example.com'),
('Pedro Oliveira', 'pedro@example.com');
"

# Tenant 2
psql -U postgres -d tenant2_db -c "
INSERT INTO clientes (nome, email) VALUES
('Alice Cooper', 'alice@example.com'),
('Bob Dylan', 'bob@example.com');
"
```

### 3. Iniciar a Aplicação

```bash
mvn spring-boot:run
```

Você verá logs como:
```
INFO com.diovanes.multitenant.MultitennantAppApplication - Starting Multitenant Application...
INFO com.diovanes.multitenant.config.DataSourceManagerConfig - Initializing DataSourceManager with tenants.yml from classpath
INFO com.diovanes.multitenant.config.DataSourceManagerConfig - DataSourceManager initialized successfully
INFO com.diovanes.multitenant.MultitennantAppApplication - Multitenant Application started successfully!
```

## Usando a API

### 1. Buscar Todos os Clientes de um Tenant

```bash
curl -X GET http://localhost:8080/api/clientes/tenant1

# Resposta esperada:
{
  "success": true,
  "tenantId": "tenant1",
  "total": 3,
  "data": [
    {
      "id": 1,
      "nome": "João Silva",
      "email": "joao@example.com"
    },
    {
      "id": 2,
      "nome": "Maria Santos",
      "email": "maria@example.com"
    },
    {
      "id": 3,
      "nome": "Pedro Oliveira",
      "email": "pedro@example.com"
    }
  ]
}
```

### 2. Buscar Cliente Específico por ID

```bash
curl -X GET http://localhost:8080/api/clientes/tenant1/1

# Resposta esperada:
{
  "success": true,
  "tenantId": "tenant1",
  "data": {
    "id": 1,
    "nome": "João Silva",
    "email": "joao@example.com"
  }
}
```

### 3. Testar com Tenant Diferente

```bash
curl -X GET http://localhost:8080/api/clientes/tenant2

# Resposta esperada:
{
  "success": true,
  "tenantId": "tenant2",
  "total": 2,
  "data": [
    {
      "id": 1,
      "nome": "Alice Cooper",
      "email": "alice@example.com"
    },
    {
      "id": 2,
      "nome": "Bob Dylan",
      "email": "bob@example.com"
    }
  ]
}
```

### 4. Testar com Tenant Inválido

```bash
curl -X GET http://localhost:8080/api/clientes/tenant_inexistente

# Resposta esperada:
{
  "success": false,
  "error": "Invalid tenant identifier",
  "message": "Invalid tenantId: tenant_inexistente"
}
```

## Arquitetura da Aplicação

```
Request HTTP
    ↓
ClienteController.java
    ↓ (chama método de service)
ClienteService.java
    ↓ (valida tenant e chama repository)
ClienteRepository.java
    ↓ (obtém datasource via MultitenantDataSourceManager)
MultitenantDataSourceManager.java
    ↓ (delega para DataSourceManager)
DataSourceManager (multitenant-datasource-hikari)
    ↓ (busca em cache ou carrega configuração)
HikariDataSource (pool de conexões)
    ↓
PostgreSQL Database (tenant-específica)
```

## Estrutura de Arquivos

```
multitennant-app/
├── src/
│   ├── main/
│   │   ├── java/com/diovanes/multitenant/
│   │   │   ├── MultitennantAppApplication.java    (main + shutdown hook)
│   │   │   ├── config/
│   │   │   │   └── DataSourceManagerConfig.java   (Spring bean config)
│   │   │   ├── controller/
│   │   │   │   └── ClienteController.java         (REST endpoints)
│   │   │   ├── service/
│   │   │   │   └── ClienteService.java            (lógica de negócio)
│   │   │   ├── repository/
│   │   │   │   ├── ClienteRepository.java         (acesso a dados)
│   │   │   │   └── MultitenantDataSourceManager.java (gerenciador de conexões)
│   │   │   └── entity/
│   │   │       └── Cliente.java                   (modelo de dados)
│   │   └── resources/
│   │       ├── application.properties             (config Spring)
│   │       └── tenants.yml                        (config de tenants)
│   └── test/
│       └── java/
├── pom.xml                                         (dependências Maven)
├── IMPLEMENTATION_UPDATE.md                        (guia de atualização)
└── USAGE_GUIDE.md                                  (este arquivo)
```

## Monitoramento e Logs

### 1. Ativar Debug Logging

Edite `src/main/resources/application.properties`:

```properties
logging.level.com.diovanes=DEBUG
logging.level.com.diovanes.datasource.multitenant=DEBUG
```

### 2. Verificar Cache Statistics

Adicione em `ClienteRepository` ou `ClienteService`:

```java
var cacheProvider = dataSourceManager.getDataSourceCache();
System.out.println(cacheProvider.getDetailedStats());
```

Output esperado:
```
Cache Stats: size=2, hits=142, misses=2, loadSuccesses=2, 
loadFailures=0, hitRate=98.62%, avgLoadPenalty=45 ms, evictions=0
```

## Troubleshooting

### Problema: "Connection refused"

**Solução:**
```bash
# Verificar se PostgreSQL está rodando
psql -U postgres -c "SELECT version();"

# Se não estiver, iniciar (macOS com brew)
brew services start postgresql@14

# Ou verificar se credenciais em tenants.yml estão corretas
```

### Problema: "No configuration found for tenant"

**Solução:**
```bash
# Verificar se tenants.yml está em src/main/resources/
ls -la src/main/resources/tenants.yml

# Verificar se está no classpath após compilação
jar tf target/multitenant-app-0.1.0.jar | grep tenants.yml

# Verificar sintaxe YAML
mvn clean compile
```

### Problema: "Table does not exist"

**Solução:**
```bash
# Conectar ao banco correto e criar tabela
psql -U postgres -d tenant1_db

\dt  # listar tabelas

CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);
```

### Problema: Aplicação inicia lentamente

**Causa comum:** Preload de tenants está tentando conectar a databases inexistentes
**Solução:** Criar todos os databases listados em tenants.yml

## Performance e Cache

### Comportamento do Cache

1. **Startup**: DataSourceManager carrega todos os tenants de tenants.yml
2. **Primeira requisição (tenant1)**: Cria HikariDataSource, 45ms (típico)
3. **Segunda requisição (tenant1)**: Reutiliza datasource, <1ms
4. **Primeira requisição (tenant2)**: Cria HikariDataSource, 45ms
5. **Requisições subsequentes**: Todas <1ms

### Expiração de Cache

- **TTL padrão**: 2 horas
- **Comportamento**: Após 2h sem uso, datasource é recriado na próxima requisição
- **Customizável**: Edite `DataSourceManagerConfig.java` para alterar TTL

## Segurança

### Boas Práticas

1. **Usar variáveis de ambiente para senhas**:

```yaml
tenants:
  tenant1:
    host: ${DB_HOST:localhost}
    port: ${DB_PORT:5432}
    user: ${DB_USER}
    password: ${DB_PASS}
    database: ${DB_NAME}
```

2. **Não commitar tenants.yml com senhas reais**:

```bash
# Adicionar a .gitignore
echo "src/main/resources/tenants.yml" >> .gitignore

# Usar tenants.yml.example para documentação
cp src/main/resources/tenants.yml src/main/resources/tenants.yml.example
```

3. **Usar SSL para conexões**:

```yaml
tenants:
  tenant1:
    # ... outras configs
    # Adicionar em HikariConfig se necessário
    sslMode: require
```

## Próximas Implementações

- [ ] Health check endpoint específico por tenant
- [ ] Métricas de cache e pool de conexões
- [ ] API de administração (criar/deletar tenants dinamicamente)
- [ ] Suporte a múltiplos bancos de dados (não apenas PostgreSQL)
- [ ] Testes de carga e performance

## Referências

- [multitenant-datasource-hikari](https://github.com/diovanes/multitenant-datasource-hikari)
- [HikariCP Documentation](https://github.com/brettwooldridge/HikariCP)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/)

