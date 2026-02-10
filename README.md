# Multitenant Application

Uma aplicação Spring Boot multi-tenant desenvolvida com Java que fornece endpoints REST para consultar dados de clientes em um banco de dados PostgreSQL, utilizando a biblioteca `multitenant-datasource-hikari` para gerenciar conexões de banco de dados em um ambiente multi-tenant.

## Arquitetura

```
src/main/java/com/diovanes/multitenant/
├── controller/         # REST Controllers
│   └── ClienteController.java
├── service/            # Business Logic Services
│   └── ClienteService.java
├── repository/         # Data Access Layer
│   ├── ClienteRepository.java
│   └── MultitenantDataSourceManager.java
├── entity/             # Domain Models
│   └── Cliente.java
└── MultitennantAppApplication.java  # Application Entry Point
```

## Características

- ✅ **Multi-tenant**: Suporte completo para múltiplos tenants com isolamento de dados
- ✅ **JdbcTemplate**: Acesso direto ao banco de dados sem ORM
- ✅ **PostgreSQL**: Banco de dados relacional PostgreSQL
- ✅ **Clean Code**: Código bem estruturado seguindo boas práticas
- ✅ **Camadas**: Separação clara entre Controller, Service e Repository
- ✅ **Logging**: Logs estruturados com SLF4J e Logback
- ✅ **REST API**: Endpoints JSON para consulta de dados
- ✅ **Sem Segurança**: Sem autenticação ou autorização (conforme requisitado)

## Tecnologias

- **Java 17+**
- **Spring Boot 3.1.7**
- **Spring JDBC (JdbcTemplate)**
- **PostgreSQL Driver 42.7.1**
- **multitenant-datasource-hikari 1.0.0**
- **SLF4J + Logback**
- **Maven 3.6+**

## Pré-requisitos

1. **Java 17+** instalado
2. **Maven 3.6+** instalado
3. **PostgreSQL 12+** instalado e rodando em `localhost:5432`
4. **Biblioteca multitenant-datasource-hikari** disponível no repositório Maven local ou remoto

## Configuração

### 1. Banco de Dados PostgreSQL

Crie o banco de dados e a tabela:

```sql
-- Criar banco de dados
CREATE DATABASE multitenant_db;

-- Conectar ao banco
\c multitenant_db

-- Criar tabela de clientes
CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

-- Inserir dados de exemplo
INSERT INTO clientes (nome, email) VALUES 
('João Silva', 'joao@example.com'),
('Maria Santos', 'maria@example.com'),
('Pedro Oliveira', 'pedro@example.com');
```

### 2. Configuração da Aplicação

Edite o arquivo `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/multitenant_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### 3. Configurar multitenant-datasource-hikari

A biblioteca `multitenant-datasource-hikari` deve ser configurada com as datasources para cada tenant. Consulte a documentação da biblioteca em: https://github.com/diovanes/multitenant-datasource-hikari

## Build e Execução

### Build do Projeto

```bash
# Limpar build anterior
mvn clean

# Compilar o projeto
mvn compile

# Executar testes
mvn test

# Criar pacote JAR
mvn package
```

### Executar a Aplicação

**Opção 1: Com Maven**
```bash
mvn spring-boot:run
```

**Opção 2: Executar JAR diretamente**
```bash
java -jar target/multitenant-app-1.0.0.jar
```

A aplicação iniciará em `http://localhost:8080`

## API REST Endpoints

### 1. Health Check
```
GET /api/clientes/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "message": "Multitenant API is running",
  "timestamp": 1707415200000
}
```

### 2. Buscar Todos os Clientes de um Tenant
```
GET /api/clientes/{tenantId}
```

**Path Parameters:**
- `tenantId` (string, required): Identificador único do tenant

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/clientes/tenant-001
```

**Response (200 OK):**
```json
{
  "success": true,
  "tenantId": "tenant-001",
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

**Response (400 Bad Request) - Tenant Inválido:**
```json
{
  "success": false,
  "error": "Invalid tenant identifier",
  "message": "Invalid tenantId: invalid-tenant"
}
```

### 3. Buscar Cliente por ID
```
GET /api/clientes/{tenantId}/{id}
```

**Path Parameters:**
- `tenantId` (string, required): Identificador único do tenant
- `id` (long, required): ID do cliente

**Example Request:**
```bash
curl -X GET http://localhost:8080/api/clientes/tenant-001/1
```

**Response (200 OK):**
```json
{
  "success": true,
  "tenantId": "tenant-001",
  "data": {
    "id": 1,
    "nome": "João Silva",
    "email": "joao@example.com"
  }
}
```

**Response (404 Not Found) - Cliente não encontrado:**
```json
{
  "success": false,
  "error": "Cliente not found",
  "tenantId": "tenant-001",
  "id": 999
}
```

## Estrutura de Código

### Entity: Cliente
```java
public class Cliente {
    private Long id;
    private String nome;
    private String email;
}
```

### Repository: ClienteRepository
- `findAllByTenant(String tenantId)`: Retorna todos os clientes de um tenant
- `findByIdAndTenant(String tenantId, Long id)`: Retorna um cliente específico

Utiliza `MultitenantDataSourceManager` para obter a conexão correta do tenant.

### Service: ClienteService
- `getAllClientesByTenant(String tenantId)`: Recupera todos os clientes
- `getClienteByIdAndTenant(String tenantId, Long id)`: Recupera um cliente específico

Valida o tenant antes de fazer requisições ao repositório.

### Controller: ClienteController
- `GET /api/clientes/{tenantId}`: Endpoint para obter todos os clientes
- `GET /api/clientes/{tenantId}/{id}`: Endpoint para obter um cliente específico
- `GET /api/clientes/health`: Endpoint de health check

## Logs

Os logs são salvos em `logs/application.log` e exibidos no console com o formato:
```
HH:mm:ss.SSS [thread] LEVEL package.class - mensagem
```

**Níveis de Log:**
- `DEBUG`: Informações detalhadas para desenvolvimento (pacote `com.diovanes.multitenant`)
- `INFO`: Informações gerais da aplicação
- `WARN`: Avisos e situações inesperadas
- `ERROR`: Erros da aplicação

## Boas Práticas Implementadas

1. **Injeção de Dependências**: Todas as dependências são injetadas via Spring
2. **Separação de Responsabilidades**: Camadas bem definidas (Controller, Service, Repository)
3. **Logging Estruturado**: Logs com contexto completo para debugging
4. **Tratamento de Exceções**: Exceções tratadas com respostas HTTP apropriadas
5. **Documentação**: Código documentado com Javadoc
6. **Validação de Entrada**: Validação de tenantId e parâmetros
7. **Nomes Descritivos**: Nomes de variáveis e métodos claros
8. **Imutabilidade**: Uso apropriado de final onde necessário
9. **Transações**: JdbcTemplate gerencia conexões automaticamente
10. **DRY Principle**: Reutilização de código (RowMapper, queries)

## Estrutura de Diretórios do Projeto

```
multitenant-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/diovanes/multitenant/
│   │   │       ├── controller/
│   │   │       │   └── ClienteController.java
│   │   │       ├── service/
│   │   │       │   └── ClienteService.java
│   │   │       ├── repository/
│   │   │       │   ├── ClienteRepository.java
│   │   │       │   └── MultitenantDataSourceManager.java
│   │   │       ├── entity/
│   │   │       │   └── Cliente.java
│   │   │       └── MultitennantAppApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
└── pom.xml
```

## Tratamento de Erros

A aplicação retorna respostas HTTP apropriadas:

- **200 OK**: Requisição bem-sucedida
- **400 Bad Request**: Parâmetros inválidos (tenantId vazio, id negativo)
- **404 Not Found**: Cliente não encontrado
- **500 Internal Server Error**: Erro ao processar requisição

Todas as respostas de erro incluem:
```json
{
  "success": false,
  "error": "Descrição do erro",
  "message": "Mensagem detalhada"
}
```

## Melhorias Futuras

1. Adicionar testes unitários e de integração
2. Implementar paginação para listas de clientes
3. Adicionar filtros e busca
4. Implementar cache para performance
5. Adicionar actuators do Spring Boot para monitoramento
6. Implementar validações com Jakarta Bean Validation
7. Adicionar documentação Swagger/OpenAPI
8. Implementar tratamento de transações explícitas

## Troubleshooting

### Erro: "Cannot get a connection"
- Verifique se PostgreSQL está rodando
- Verifique credenciais de banco de dados em `application.properties`
- Verifique se o banco `multitenant_db` existe

### Erro: "DataSource not found for tenantId"
- Verifique se `multitenant-datasource-hikari` está configurado corretamente
- Verifique se o tenant existe nas configurações

### Erro: "Cannot find symbol 'MultitenantDataSourceProvider'"
- Certifique-se de que a biblioteca `multitenant-datasource-hikari` está no classpath
- Verifique o pom.xml e execute `mvn clean install`

## Contribuindo

1. Faça fork do repositório
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT.

## Contato

Para dúvidas ou sugestões, entre em contato através do GitHub.
