# Docker Setup Guide - PostgreSQL

Este guia explica como usar o Docker para configurar e executar um banco de dados PostgreSQL para a aplicação Multitenant.

## Pré-requisitos

1. **Docker Desktop instalado**
   - macOS: https://www.docker.com/products/docker-desktop
   - Linux: `sudo apt-get install docker.io`
   - Windows: https://www.docker.com/products/docker-desktop

2. **Docker Compose instalado** (geralmente vem com Docker Desktop)
   ```bash
   docker-compose --version
   ```

## Opção 1: Usar Docker Compose (RECOMENDADO)

A forma mais fácil de levantar o PostgreSQL com todas as configurações necessárias.

### Iniciar o PostgreSQL com Docker Compose

```bash
cd /Users/diovaneschumann/git/multitennant-app

# Iniciar os serviços (PostgreSQL + PgAdmin)
docker-compose up -d

# Verificar se está rodando
docker-compose ps
```

**Saída esperada:**
```
NAME                         COMMAND                  SERVICE      STATUS
multitenant-postgres-db      postgres                 postgres     Up (healthy)
multitenant-pgadmin          /entrypoint.sh           pgadmin      Up
```

### Acessar o banco de dados

**Linha de comando:**
```bash
# Conectar ao PostgreSQL via psql
docker-compose exec postgres psql -U postgres -d multitenant_db

# Verificar tabelas
\dt

# Ver clientes
SELECT * FROM clientes;

# Sair
\q
```

**GUI - PgAdmin:**
- URL: http://localhost:5050
- Email: admin@example.com
- Senha: admin

### Parar os serviços

```bash
# Parar temporariamente
docker-compose stop

# Remover containers (dados persistem em volumes)
docker-compose down

# Remover containers e volumes (APAGA DADOS)
docker-compose down -v
```

## Opção 2: Usar Dockerfile Manualmente

Para maior controle sobre o container.

### Build da imagem

```bash
cd /Users/diovaneschumann/git/multitennant-app

# Build da imagem
docker build -t multitenant-postgres:latest .

# Verificar imagem criada
docker images | grep multitenant-postgres
```

### Executar o container

```bash
# Iniciar container
docker run -d \
  --name multitenant-db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -v multitenant_postgres_data:/var/lib/postgresql/data \
  multitenant-postgres:latest

# Verificar se está rodando
docker ps | grep multitenant-db
```

### Ver logs do container

```bash
# Ver logs em tempo real
docker logs -f multitenant-db

# Ver últimas 50 linhas
docker logs --tail 50 multitenant-db
```

### Conectar ao banco

```bash
# Via docker exec
docker exec -it multitenant-db psql -U postgres -d multitenant_db

# Via psql localmente (se instalado)
psql -h localhost -U postgres -d multitenant_db
```

### Parar o container

```bash
# Parar container
docker stop multitenant-db

# Remover container (mantém volume)
docker rm multitenant-db

# Remover container e volume (apaga dados)
docker rm -v multitenant-db
docker volume rm multitenant_postgres_data
```

## Testar Conexão da Aplicação Spring Boot

Após subir o PostgreSQL (seja via Compose ou Manual):

### 1. Configurar application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/multitenant_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
```

### 2. Executar a aplicação

```bash
mvn spring-boot:run
```

### 3. Testar endpoint

```bash
curl -X GET http://localhost:8080/api/clientes/tenant-001
```

## Configuração de Conexão Externa (Fora do Docker)

O PostgreSQL está configurado para aceitar conexões de qualquer host (0.0.0.0).

### Conectar de outro computador na rede

```bash
# Assumindo que seu servidor Docker está em 192.168.1.100
psql -h 192.168.1.100 -U postgres -d multitenant_db
```

### Configurações de Firewall

Se estiver em um servidor remoto, certifique-se de que a porta 5432 está aberta:

```bash
# Linux - UFW
sudo ufw allow 5432

# Linux - Firewall-cmd
sudo firewall-cmd --add-port=5432/tcp --permanent
sudo firewall-cmd --reload

# macOS - Não tem firewall por padrão para portas internas
```

## Banco de Dados Criados

O script init-db.sql cria automaticamente:

### 1. multitenant_db
- Tabela: `clientes` com 10 registros de exemplo
- Índices para email, nome e data de criação
- Usuário: `postgres`
- Senha: `postgres`

### 2. tenant_001_db (Multi-tenant)
- Tabela: `clientes` com 3 registros
- Dados isolados para tenant-001

### 3. tenant_002_db (Multi-tenant)
- Tabela: `clientes` com 3 registros
- Dados isolados para tenant-002

## Variáveis de Ambiente

### PostgreSQL

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| POSTGRES_USER | postgres | Usuário admin do PostgreSQL |
| POSTGRES_PASSWORD | postgres | Senha do usuário |
| POSTGRES_DB | multitenant_db | Banco principal |
| POSTGRES_INITDB_ARGS | -c listen_addresses='*' | Aceita conexões externas |

### PgAdmin

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| PGADMIN_DEFAULT_EMAIL | admin@example.com | Email do admin |
| PGADMIN_DEFAULT_PASSWORD | admin | Senha do admin |

## Troubleshooting

### Erro: "Cannot connect to Docker daemon"

**Causa:** Docker não está rodando

**Solução:**
```bash
# macOS
open /Applications/Docker.app

# Linux
sudo systemctl start docker

# Windows - Abra Docker Desktop
```

### Erro: "Port 5432 is already allocated"

**Causa:** Porta já está em uso

**Solução:**
```bash
# Listar containers usando porta 5432
docker ps -a | grep 5432

# Parar container existente
docker stop <container_id>

# Ou usar porta diferente
docker run -p 5433:5432 ...
```

### Erro: "Database multitenant_db does not exist"

**Causa:** Script init-db.sql não foi executado

**Solução:**
```bash
# Para docker-compose
docker-compose down -v
docker-compose up -d

# Para container manual
docker exec multitenant-db psql -U postgres -f /docker-entrypoint-initdb.d/01-init.sql
```

### Banco de dados não tem dados

**Causa:** Script init-db.sql não executou corretamente

**Solução:**
```bash
# Verificar logs
docker logs multitenant-db

# Importar dados manualmente
docker exec -it multitenant-db psql -U postgres -d multitenant_db -c "INSERT INTO clientes (nome, email) VALUES ('Test', 'test@example.com');"
```

## Backup e Restore

### Fazer Backup

```bash
# Backup de um banco específico
docker exec multitenant-db pg_dump -U postgres -d multitenant_db > backup_multitenant.sql

# Backup de todos os bancos
docker exec multitenant-db pg_dumpall -U postgres > backup_full.sql
```

### Restaurar Backup

```bash
# Restaurar banco específico
docker exec -i multitenant-db psql -U postgres -d multitenant_db < backup_multitenant.sql

# Restaurar todos os bancos
docker exec -i multitenant-db psql -U postgres < backup_full.sql
```

## Performance e Recursos

### Ajustar limite de memória

```bash
# No docker-compose.yml
services:
  postgres:
    mem_limit: 2g
    cpus: '2.0'
```

### Ajustar configurações do PostgreSQL

```bash
# No Dockerfile ou via environment
ENV POSTGRES_INITDB_ARGS="-c max_connections=200 -c shared_buffers=256MB"
```

## Segurança

### AVISO: Credenciais padrão para desenvolvimento

As credenciais padrão (`postgres:postgres`) são apenas para **desenvolvimento local**.

Para **produção**, altere:

```yaml
# docker-compose.yml
services:
  postgres:
    environment:
      POSTGRES_USER: seu_usuario
      POSTGRES_PASSWORD: sua_senha_segura
```

### Limitar acesso à rede

```yaml
# docker-compose.yml - Apenas localhost
ports:
  - "127.0.0.1:5432:5432"
```

## Próximos Passos

1. ✅ Subir PostgreSQL com Docker
2. ⏭️ Configurar aplicação Spring Boot para conectar
3. ⏭️ Executar aplicação
4. ⏭️ Testar endpoints

Veja [RUNNING.md](RUNNING.md) para continuação.

## Referências

- Docker Official PostgreSQL: https://hub.docker.com/_/postgres
- Docker Compose Documentation: https://docs.docker.com/compose/
- PgAdmin Documentation: https://www.pgadmin.org/
- PostgreSQL Documentation: https://www.postgresql.org/docs/

---

**Última atualização:** 9 de fevereiro de 2026
