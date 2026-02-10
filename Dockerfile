# Dockerfile para PostgreSQL com suporte a multi-tenant
# Build: docker build -t multitenant-postgres:latest .
# Run: docker run -d -p 5432:5432 --name multitenant-db multitenant-postgres:latest

FROM postgres:16-alpine

# Variáveis de ambiente para PostgreSQL
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV POSTGRES_INITDB_ARGS="-c listen_addresses='*'"

# Label para identificação
LABEL maintainer="Diovanes Schumann"
LABEL description="PostgreSQL Database for Multitenant Application"
LABEL version="1.0"

# Expor porta 5432 para acesso externo
EXPOSE 5432

# Copiar configuração do PostgreSQL para aceitar conexões externas
RUN echo "host all all 0.0.0.0/0 md5" >> /var/lib/postgresql/data/pg_hba.conf || true

# Copiar scripts de inicialização
COPY ./init-db.sql /docker-entrypoint-initdb.d/01-init.sql

# Volume para persistência de dados
VOLUME ["/var/lib/postgresql/data"]

# Comando padrão (será sobrescrito pelos parâmetros do container)
CMD ["postgres"]
