# Docker Setup Completo - PostgreSQL

✅ **Dockerização completa da aplicação PostgreSQL foi implementada com sucesso!**

## 📦 Arquivos Docker Criados

1. **Dockerfile** - Imagem PostgreSQL 16 Alpine
2. **docker-compose.yml** - Orquestração de containers (PostgreSQL + PgAdmin)
3. **init-db.sql** - Script de inicialização com dados de exemplo
4. **docker-helper.sh** - Script auxiliar com 15+ comandos úteis
5. **.dockerignore** - Arquivo para otimizar build

## 🚀 Quick Start - 3 Passos

### 1️⃣ Iniciar PostgreSQL

```bash
cd /Users/diovaneschumann/git/multitennant-app
docker-compose up -d
```

### 2️⃣ Verificar Status

```bash
docker-compose ps
```

Esperado:
```
NAME                      STATUS
multitenant-postgres-db   Up (healthy)
multitenant-pgadmin       Up
```

### 3️⃣ Conectar ao Banco

```bash
# Via Docker (sem psql instalado)
docker-compose exec postgres psql -U postgres -d multitenant_db

# Via linha de comando (se psql instalado)
psql -h localhost -U postgres -d multitenant_db
```

## 📋 O que foi Criado Automaticamente

### Banco de Dados
- ✅ **multitenant_db** - Banco principal com tabela 'clientes' (10 registros)
- ✅ **tenant_001_db** - Dados isolados para tenant-001 (3 registros)
- ✅ **tenant_002_db** - Dados isolados para tenant-002 (3 registros)

### Tabelas
- Coluna `id` (BIGSERIAL PRIMARY KEY)
- Coluna `nome` (VARCHAR 255)
- Coluna `email` (VARCHAR 255 UNIQUE)
- Coluna `created_at` (TIMESTAMP)
- Coluna `updated_at` (TIMESTAMP)

### Índices
- idx_clientes_email
- idx_clientes_nome
- idx_clientes_created_at

### Acessos
- ✅ Aceita conexões de qualquer host (0.0.0.0/0)
- ✅ Usuário: `postgres`
- ✅ Senha: `postgres`
- ✅ Porta: `5432`

## 🛠️ Docker Helper Script

Script shell com 15+ comandos para facilitar operações:

```bash
./docker-helper.sh start      # Iniciar PostgreSQL
./docker-helper.sh status     # Ver status
./docker-helper.sh logs       # Ver logs
./docker-helper.sh exec       # Conectar ao banco
./docker-helper.sh health     # Verificar saúde
./docker-helper.sh backup     # Fazer backup
./docker-helper.sh restore    # Restaurar backup
./docker-helper.sh clients    # Ver todos os clientes
./docker-helper.sh stop       # Parar PostgreSQL
./docker-helper.sh help       # Ver todos os comandos
```

## 🔧 Configuração

### Variáveis de Ambiente

**PostgreSQL:**
```yaml
POSTGRES_USER: postgres
POSTGRES_PASSWORD: postgres
POSTGRES_INITDB_ARGS: "-c listen_addresses='*'"
```

**PgAdmin (GUI):**
```yaml
PGADMIN_DEFAULT_EMAIL: admin@example.com
PGADMIN_DEFAULT_PASSWORD: admin
```

## 🌐 Acesso Externo

### Desde a Máquina Local

```bash
# Via psql (se instalado)
psql -h localhost -U postgres -d multitenant_db

# Via Java/Spring Boot
spring.datasource.url=jdbc:postgresql://localhost:5432/multitenant_db
```

### De Outro Computador na Rede

```bash
# Assumindo servidor Docker em 192.168.1.100
psql -h 192.168.1.100 -U postgres -d multitenant_db

# Spring Boot
spring.datasource.url=jdbc:postgresql://192.168.1.100:5432/multitenant_db
```

## 📊 Integração com Aplicação Spring Boot

### Passo 1: Atualizar application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/multitenant_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
```

### Passo 2: Iniciar aplicação

```bash
mvn spring-boot:run
```

### Passo 3: Testar

```bash
curl http://localhost:8080/api/clientes/tenant-001
```

## 📈 Interface Web - PgAdmin

- URL: **http://localhost:5050**
- Email: **admin@example.com**
- Senha: **admin**

PgAdmin permite gerenciar PostgreSQL via interface web gráfica.

## 🔐 Segurança

⚠️ **AVISO:** Credenciais padrão são apenas para **desenvolvimento local**.

Para produção, altere em `docker-compose.yml`:

```yaml
services:
  postgres:
    environment:
      POSTGRES_PASSWORD: sua_senha_segura
  pgadmin:
    environment:
      PGADMIN_DEFAULT_PASSWORD: admin_senha_segura
```

Ou restrinja acesso:

```yaml
ports:
  - "127.0.0.1:5432:5432"  # Apenas localhost
```

## 📚 Comandos Frequentes

### Operações Básicas

```bash
# Iniciar
docker-compose up -d

# Parar
docker-compose stop

# Remover (com dados)
docker-compose down

# Remover (sem dados)
docker-compose down -v

# Ver status
docker-compose ps

# Ver logs
docker-compose logs -f postgres
```

### Gerenciar Banco

```bash
# Conectar
docker-compose exec postgres psql -U postgres -d multitenant_db

# Listar bancos
docker-compose exec postgres psql -U postgres -l

# Contar registros
docker-compose exec postgres psql -U postgres -d multitenant_db -c "SELECT COUNT(*) FROM clientes;"

# Ver clientes
docker-compose exec postgres psql -U postgres -d multitenant_db -c "SELECT * FROM clientes;"
```

### Backup & Restore

```bash
# Backup
docker exec multitenant-postgres-db pg_dump -U postgres -d multitenant_db > backup.sql

# Restore
docker exec -i multitenant-postgres-db psql -U postgres -d multitenant_db < backup.sql

# Backup via helper
./docker-helper.sh backup

# Restore via helper
./docker-helper.sh restore backup_20260209_120000.sql
```

## 🐛 Troubleshooting

### Porta 5432 em uso

```bash
# Listar processo usando porta 5432
lsof -i :5432

# Parar container existente
docker-compose stop
```

### Banco não tem dados

```bash
# Remover e reiniciar
docker-compose down -v
docker-compose up -d
```

### Conexão recusada

```bash
# Verificar saúde
docker-compose exec postgres pg_isready -U postgres

# Ver logs
docker-compose logs postgres
```

### Espaço em disco

```bash
# Limpar images não utilizadas
docker image prune -f

# Limpar volumes não utilizados
docker volume prune -f

# Usar helper
./docker-helper.sh clean
```

## 📝 Arquivos de Referência

| Arquivo | Propósito |
|---------|-----------|
| [Dockerfile](Dockerfile) | Define imagem PostgreSQL 16 Alpine |
| [docker-compose.yml](docker-compose.yml) | Orquestra PostgreSQL + PgAdmin |
| [init-db.sql](init-db.sql) | Script de inicialização (bancos + tabelas + dados) |
| [docker-helper.sh](docker-helper.sh) | Script auxiliar com 15+ comandos |
| [.dockerignore](.dockerignore) | Arquivos ignorados no build |
| [DOCKER.md](DOCKER.md) | Documentação completa (este arquivo) |

## 🎯 Próximos Passos

1. ✅ Subir PostgreSQL com `docker-compose up -d`
2. ✅ Verificar com `docker-compose ps`
3. ⏭️ Conectar aplicação Spring Boot
4. ⏭️ Testar endpoints
5. ⏭️ Fazer backup dos dados

## 💡 Dicas

### Performance

Para ativar recursos no Dockerfile:

```dockerfile
ENV POSTGRES_INITDB_ARGS="-c max_connections=200 -c shared_buffers=256MB"
```

### Persistência

Dados são salvos em volume Docker:
```bash
docker volume ls | grep multitenant
```

### Rebuild Rápido

```bash
docker-compose up -d --build
```

## 📞 Referências

- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)
- [Docker Compose Docs](https://docs.docker.com/compose/)
- [PgAdmin Docs](https://www.pgadmin.org/)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)

---

**Status:** ✅ Pronto para Uso  
**Data:** 9 de fevereiro de 2026  
**Versão:** 1.0
