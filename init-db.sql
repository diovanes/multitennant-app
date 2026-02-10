-- =====================================================
-- Inicialização do Banco de Dados PostgreSQL
-- Para Multitenant Application
-- =====================================================

-- =====================================================
-- Criar usuário com todos os acessos (opcional)
-- =====================================================
-- CREATE USER multitenant_user WITH PASSWORD 'multitenant_pass';
-- ALTER ROLE multitenant_user SUPERUSER;

-- =====================================================
-- Criar banco de dados multitenant_db
-- =====================================================
CREATE DATABASE multitenant_db
    WITH ENCODING 'UTF8'
    LC_COLLATE 'C'
    LC_CTYPE 'C'
    TEMPLATE = template0;

-- =====================================================
-- Conectar ao banco multitenant_db
-- =====================================================
\c multitenant_db

-- =====================================================
-- Criar tabela clientes
-- =====================================================
CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- Criar índices para melhor performance
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_clientes_email ON clientes(email);
CREATE INDEX IF NOT EXISTS idx_clientes_nome ON clientes(nome);
CREATE INDEX IF NOT EXISTS idx_clientes_created_at ON clientes(created_at);

-- =====================================================
-- Inserir dados de exemplo
-- =====================================================
INSERT INTO clientes (nome, email) VALUES 
('João Silva', 'joao.silva@example.com'),
('Maria Santos', 'maria.santos@example.com'),
('Pedro Oliveira', 'pedro.oliveira@example.com'),
('Ana Costa', 'ana.costa@example.com'),
('Carlos Mendes', 'carlos.mendes@example.com'),
('Fernanda Lopez', 'fernanda.lopez@example.com'),
('Ricardo Alves', 'ricardo.alves@example.com'),
('Juliana Rocha', 'juliana.rocha@example.com'),
('Bruno Costa', 'bruno.costa@example.com'),
('Camila Souza', 'camila.souza@example.com')
ON CONFLICT (email) DO NOTHING;

-- =====================================================
-- Grant de permissões para o usuário postgres
-- =====================================================
GRANT CONNECT ON DATABASE multitenant_db TO postgres;
GRANT USAGE ON SCHEMA public TO postgres;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO postgres;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO postgres;

-- =====================================================
-- Verificar dados inseridos
-- =====================================================
SELECT COUNT(*) as total_clientes FROM clientes;

-- =====================================================
-- Criar banco para tenant-001 (opcional)
-- =====================================================
CREATE DATABASE tenant_001_db
    WITH ENCODING 'UTF8'
    TEMPLATE = template0;

\c tenant_001_db

CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_clientes_email ON clientes(email);

INSERT INTO clientes (nome, email) VALUES 
('João Silva Tenant 001', 'joao.tenant001@example.com'),
('Maria Santos Tenant 001', 'maria.tenant001@example.com'),
('Pedro Oliveira Tenant 001', 'pedro.tenant001@example.com')
ON CONFLICT (email) DO NOTHING;

GRANT CONNECT ON DATABASE tenant_001_db TO postgres;
GRANT USAGE ON SCHEMA public TO postgres;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO postgres;

-- =====================================================
-- Criar banco para tenant-002 (opcional)
-- =====================================================
CREATE DATABASE tenant_002_db
    WITH ENCODING 'UTF8'
    TEMPLATE = template0;

\c tenant_002_db

CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_clientes_email ON clientes(email);

INSERT INTO clientes (nome, email) VALUES 
('Alice Costa Tenant 002', 'alice.tenant002@example.com'),
('Bob Martinez Tenant 002', 'bob.tenant002@example.com'),
('Carol Davis Tenant 002', 'carol.tenant002@example.com')
ON CONFLICT (email) DO NOTHING;

GRANT CONNECT ON DATABASE tenant_002_db TO postgres;
GRANT USAGE ON SCHEMA public TO postgres;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO postgres;

-- =====================================================
-- Listar bancos de dados criados
-- =====================================================
\l

-- =====================================================
-- FIM DO SCRIPT DE INICIALIZAÇÃO
-- =====================================================
