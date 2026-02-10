# Guia de Execução - Multitenant Application

Este documento fornece um guia passo a passo para executar a aplicação Multitenant Spring Boot.

## Pré-requisitos

Antes de começar, certifique-se de que você tem os seguintes softwares instalados:

1. **Java 17 ou superior**
   ```bash
   java -version
   ```

2. **Maven 3.6 ou superior**
   ```bash
   mvn -version
   ```

3. **PostgreSQL 12 ou superior**
   ```bash
   psql --version
   ```

4. **Git (opcional, para versionamento)**
   ```bash
   git --version
   ```

## Passo 1: Preparar o Banco de Dados PostgreSQL

### 1.1 Iniciar PostgreSQL

**No macOS com Homebrew:**
```bash
brew services start postgresql
```

**No Linux:**
```bash
sudo service postgresql start
```

**No Windows:**
Use PostgreSQL Services ou execute pgAdmin.

### 1.2 Conectar ao PostgreSQL

```bash
psql -U postgres
```

Se solicitado pela senha, digite a senha do usuário postgres.

### 1.3 Executar o Script SQL

```bash
psql -U postgres -f database-setup.sql
```

Ou manualmente no prompt do PostgreSQL:

```sql
CREATE DATABASE multitenant_db;

\c multitenant_db

CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO clientes (nome, email) VALUES 
('João Silva', 'joao.silva@example.com'),
('Maria Santos', 'maria.santos@example.com'),
('Pedro Oliveira', 'pedro.oliveira@example.com'),
('Ana Costa', 'ana.costa@example.com'),
('Carlos Mendes', 'carlos.mendes@example.com');

SELECT * FROM clientes;
```

### 1.4 Verificar a Criação

```bash
psql -U postgres -d multitenant_db -c "SELECT * FROM clientes;"
```

## Passo 2: Configurar a Aplicação

### 2.1 Ajustar application.properties (se necessário)

Edite `src/main/resources/application.properties`:

```properties
# Se seu PostgreSQL usa porta diferente de 5432
spring.datasource.url=jdbc:postgresql://localhost:5432/multitenant_db

# Se o usuário PostgreSQL é diferente de 'postgres'
spring.datasource.username=postgres

# Se a senha é diferente
spring.datasource.password=postgres
```

### 2.2 Configurar multitenant-datasource-hikari

A biblioteca `multitenant-datasource-hikari` precisa ser configurada com as datasources para cada tenant.

Você pode fazer isso de várias formas:
- Via properties file
- Via arquivo de configuração externo
- Via código Java
- Via API de configuração da biblioteca

Consulte a documentação: https://github.com/diovanes/multitenant-datasource-hikari

## Passo 3: Compilar a Aplicação

```bash
# Navegar para o diretório do projeto
cd /Users/diovaneschumann/git/multitennant-app

# Limpar builds anteriores
mvn clean

# Compilar o projeto
mvn compile
```

**Saída esperada:**
```
[INFO] BUILD SUCCESS
```

## Passo 4: Executar Testes (Opcional)

```bash
mvn test
```

## Passo 5: Gerar Pacote JAR

```bash
mvn package
```

Isso criará um arquivo `target/multitenant-app-1.0.0.jar`

## Passo 6: Executar a Aplicação

### Opção 1: Com Maven (Recomendado para Desenvolvimento)

```bash
mvn spring-boot:run
```

**Saída esperada:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_|\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.7)

Starting Multitenant Application...
Multitenant Application started successfully!
```

### Opção 2: Executar JAR Diretamente

```bash
java -jar target/multitenant-app-1.0.0.jar
```

### Opção 3: Executar com Variáveis de Ambiente

```bash
java -Dspring.datasource.url=jdbc:postgresql://localhost:5432/multitenant_db \
     -Dspring.datasource.username=postgres \
     -Dspring.datasource.password=postgres \
     -jar target/multitenant-app-1.0.0.jar
```

## Passo 7: Verificar se a Aplicação Está Rodando

A aplicação estará disponível em `http://localhost:8080`

### Health Check

```bash
curl -X GET http://localhost:8080/api/clientes/health
```

**Resposta esperada:**
```json
{
  "status": "UP",
  "message": "Multitenant API is running",
  "timestamp": 1707415200000
}
```

## Passo 8: Testar os Endpoints

### 8.1 Buscar Todos os Clientes

```bash
curl -X GET http://localhost:8080/api/clientes/tenant-001
```

**Resposta esperada:**
```json
{
  "success": true,
  "tenantId": "tenant-001",
  "total": 5,
  "data": [
    {
      "id": 1,
      "nome": "João Silva",
      "email": "joao.silva@example.com"
    },
    {
      "id": 2,
      "nome": "Maria Santos",
      "email": "maria.santos@example.com"
    },
    {
      "id": 3,
      "nome": "Pedro Oliveira",
      "email": "pedro.oliveira@example.com"
    },
    {
      "id": 4,
      "nome": "Ana Costa",
      "email": "ana.costa@example.com"
    },
    {
      "id": 5,
      "nome": "Carlos Mendes",
      "email": "carlos.mendes@example.com"
    }
  ]
}
```

### 8.2 Buscar Cliente Específico

```bash
curl -X GET http://localhost:8080/api/clientes/tenant-001/1
```

**Resposta esperada:**
```json
{
  "success": true,
  "tenantId": "tenant-001",
  "data": {
    "id": 1,
    "nome": "João Silva",
    "email": "joao.silva@example.com"
  }
}
```

### 8.3 Testar com Different Tenants

Para testar com múltiplos tenants, certifique-se de que a biblioteca `multitenant-datasource-hikari` está configurada com múltiplas datasources para tenants diferentes:

```bash
# Tenant 1
curl -X GET http://localhost:8080/api/clientes/tenant-001

# Tenant 2
curl -X GET http://localhost:8080/api/clientes/tenant-002
```

## Passo 9: Parar a Aplicação

Para parar a aplicação, pressione `Ctrl+C` no terminal.

## Troubleshooting

### Problema: "Refused to connect" ao conectar ao banco de dados

**Solução:**
1. Verifique se PostgreSQL está rodando:
   ```bash
   psql -U postgres -d postgres -c "SELECT version();"
   ```

2. Verifique as credenciais em `application.properties`

3. Verifique se o banco `multitenant_db` existe:
   ```bash
   psql -U postgres -l | grep multitenant_db
   ```

### Problema: "Cannot find class 'MultitenantDataSourceProvider'"

**Solução:**
1. Verifique se `multitenant-datasource-hikari` está no pom.xml
2. Execute `mvn clean install` novamente
3. Verifique se a biblioteca está no repositório Maven

### Problema: Porta 8080 já está em uso

**Solução:**
Altere a porta em `application.properties`:
```properties
server.port=8081
```

Ou execute com variável de ambiente:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Problema: Cliente não encontrado

**Verificação:**
1. Certifique-se de que o tenant está configurado na biblioteca multitenant-datasource-hikari
2. Verifique os dados no banco de dados:
   ```bash
   psql -U postgres -d multitenant_db -c "SELECT * FROM clientes WHERE id = 1;"
   ```

## Análise de Logs

Os logs são salvos em `logs/application.log`:

```bash
# Ver logs em tempo real
tail -f logs/application.log

# Buscar erros
grep "ERROR" logs/application.log

# Buscar avisos
grep "WARN" logs/application.log
```

## Próximos Passos

1. Integrar a aplicação em seu pipeline CI/CD
2. Configurar Docker para containerizar a aplicação
3. Implementar testes automatizados
4. Adicionar documentação Swagger/OpenAPI
5. Implementar cache distribuído
6. Configurar monitoramento e alertas

## Suporte

Para dúvidas ou problemas, consulte:
- README.md: Documentação geral da aplicação
- Documentação da biblioteca: https://github.com/diovanes/multitenant-datasource-hikari
- Documentação do Spring Boot: https://spring.io/projects/spring-boot
