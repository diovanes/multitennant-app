# 🚀 Quick Reference - Multitenant App

## ⚡ Comandos Rápidos

### Build e Execução

```bash
# Compilar
mvn clean compile

# Build (sem testes)
mvn clean package -DskipTests

# Build com testes
mvn clean verify

# Executar
mvn spring-boot:run

# Build JAR executável
java -jar target/multitenant-app-0.1.0.jar
```

### Banco de Dados

```bash
# Criar databases
createdb tenant1_db
createdb tenant2_db

# Criar tabelas
psql -d tenant1_db << EOF
CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);
EOF

# Inserir dados
psql -d tenant1_db -c "INSERT INTO clientes (nome, email) VALUES ('João Silva', 'joao@example.com');"

# Verificar dados
psql -d tenant1_db -c "SELECT * FROM clientes;"
```

---

## 🔧 Configuração Rápida

### tenants.yml

Edite `src/main/resources/tenants.yml`:

```yaml
tenants:
  tenant1:
    host: localhost          # Host do BD
    port: 5432              # Porta
    user: postgres          # Usuário
    password: senha         # Senha
    database: tenant1_db    # Database
    schema: public          # Schema
    poolSize: 10            # Tamanho pool
    connectionTimeoutMs: 30000  # Timeout
```

---

## 📡 API Endpoints

### Listar Clientes

```bash
# GET todos os clientes de um tenant
curl http://localhost:8080/api/clientes/tenant1

# Resposta:
{
  "success": true,
  "tenantId": "tenant1",
  "total": 3,
  "data": [...]
}
```

### Buscar Cliente por ID

```bash
# GET cliente específico
curl http://localhost:8080/api/clientes/tenant1/1

# Resposta:
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

---

## 📂 Estrutura de Arquivos

```
src/main/
├── java/com/diovanes/multitenant/
│   ├── MultitennantAppApplication.java          # Main
│   ├── config/DataSourceManagerConfig.java      # Spring Bean
│   ├── controller/ClienteController.java        # REST API
│   ├── service/ClienteService.java              # Lógica
│   ├── repository/
│   │   ├── ClienteRepository.java               # Acesso dados
│   │   └── MultitenantDataSourceManager.java    # Pool manager
│   └── entity/Cliente.java                      # Modelo
└── resources/
    ├── application.properties                   # Config Spring
    └── tenants.yml                              # Config tenants
```

---

## 🔐 Logs Importantes

### Startup Normal

```
INFO ... MultitennantAppApplication - Starting Multitenant Application...
INFO ... DataSourceManagerConfig - Initializing DataSourceManager...
INFO ... DataSourceManagerConfig - DataSourceManager initialized successfully
INFO ... MultitennantAppApplication - Multitenant Application started successfully!
```

### Ativar Debug

```properties
# application.properties
logging.level.com.diovanes=DEBUG
logging.level.com.diovanes.datasource.multitenant=DEBUG
```

---

## 🧪 Testes Rápidos

### Verificar Status

```bash
# Todos os clientes
curl -s http://localhost:8080/api/clientes/tenant1 | jq .

# Cliente específico
curl -s http://localhost:8080/api/clientes/tenant1/1 | jq .

# Tenant inválido (deve retornar erro)
curl -s http://localhost:8080/api/clientes/invalid | jq .
```

### Inserir Dados de Teste

```bash
psql -d tenant1_db << EOF
INSERT INTO clientes (nome, email) VALUES
('João Silva', 'joao@example.com'),
('Maria Santos', 'maria@example.com'),
('Pedro Oliveira', 'pedro@example.com');
EOF
```

---

## ⚙️ Configuração Spring

### application.properties

```properties
# Port
server.port=8080

# Logging
logging.level.root=INFO
logging.level.com.diovanes=DEBUG
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# Charset
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
```

---

## 🐛 Troubleshooting

### Erro: Connection refused

```bash
# Verificar se PostgreSQL está rodando
psql -U postgres -c "SELECT version();"

# Iniciar PostgreSQL (macOS)
brew services start postgresql@14

# Verificar credenciais em tenants.yml
```

### Erro: Table does not exist

```bash
# Criar tabela
psql -d tenant1_db << EOF
CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);
EOF
```

### Erro: No configuration found for tenant

```bash
# Verificar se tenant existe em tenants.yml
grep "tenant1:" src/main/resources/tenants.yml

# Recompilar (resource pode não estar no classpath)
mvn clean compile
```

---

## 📊 Monitoramento

### Cache Statistics

```java
// No code:
var cacheProvider = dataSourceManager.getDataSourceCache();
System.out.println(cacheProvider.getDetailedStats());

// Output:
// Cache Stats: size=2, hits=142, misses=2, loadSuccesses=2,
// loadFailures=0, hitRate=98.62%, avgLoadPenalty=45 ms, evictions=0
```

### Verificar Pool Status

```bash
# Logs mostrarão:
# - Número de conexões abertas
# - Pool size por tenant
# - Tempo de criação de datasource
```

---

## 🔄 Fluxo de Requisição

```
HTTP Request: GET /api/clientes/tenant1
          ↓
ClienteController.getAllClientes("tenant1")
          ↓
ClienteService.getAllClientesByTenant("tenant1")
          ↓ Valida tenant
ClienteRepository.findAllByTenant("tenant1")
          ↓
MultitenantDataSourceManager.getDataSource("tenant1")
          ↓
DataSourceManager.getDataSource("tenant1")
          ↓ Busca em cache (Caffeine)
          ↓ ou cria novo HikariDataSource
JdbcTemplate.query(SQL)
          ↓
ResultSet → List<Cliente>
          ↓
ClienteController → JSON Response
```

---

## 📚 Arquivos de Documentação

| Arquivo | Conteúdo |
|---------|----------|
| `USAGE_GUIDE.md` | Guia completo de uso |
| `IMPLEMENTATION_UPDATE.md` | Detalhes técnicos da atualização |
| `UPDATE_SUMMARY.md` | Resumo executivo |
| `QUICK_REFERENCE.md` | Este arquivo |

---

## 💾 Versões

- **Java**: 17+
- **Spring Boot**: 3.1.7
- **Maven**: 3.9.0+
- **HikariCP**: 5.1.0
- **PostgreSQL Driver**: 42.7.1
- **multitenant-datasource-hikari**: 0.1.0
- **Caffeine Cache**: 3.1.8
- **SnakeYAML**: 2.2

---

## 📞 Suporte

### Logs Principais

```bash
# Ver logs da aplicação
tail -f logs/application.log

# Compilar com verbosidade
mvn -X clean compile

# Debug de conexão PostgreSQL
psql -d tenant1_db -v ON_ERROR_STOP=1
```

### Maven Troubleshooting

```bash
# Limpar cache local
rm -rf ~/.m2/repository/com/diovanes

# Forçar download de dependências
mvn clean dependency:resolve

# Ver árvore de dependências
mvn dependency:tree
```

---

**Keep it simple, keep it clean! 🧹**

