-- =====================================================
-- SQL Script: Database Setup for Multitenant Application
-- Database: PostgreSQL
-- Description: Creates the initial database and tables
-- =====================================================

-- Create Database
CREATE DATABASE multitenant_db
    WITH ENCODING 'UTF8'
    TEMPLATE = template0;

-- Connect to the database
-- Note: You need to connect separately using:
-- psql -U postgres -d multitenant_db

-- =====================================================
-- Create clientes table
-- =====================================================
CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on email for better query performance
CREATE INDEX IF NOT EXISTS idx_clientes_email ON clientes(email);
CREATE INDEX IF NOT EXISTS idx_clientes_nome ON clientes(nome);

-- =====================================================
-- Insert Sample Data
-- =====================================================
INSERT INTO clientes (nome, email) VALUES 
('João Silva', 'joao.silva@example.com'),
('Maria Santos', 'maria.santos@example.com'),
('Pedro Oliveira', 'pedro.oliveira@example.com'),
('Ana Costa', 'ana.costa@example.com'),
('Carlos Mendes', 'carlos.mendes@example.com')
ON CONFLICT (email) DO NOTHING;

-- =====================================================
-- Verify Insert
-- =====================================================
SELECT * FROM clientes;

-- =====================================================
-- Grant Permissions (optional)
-- =====================================================
-- GRANT CONNECT ON DATABASE multitenant_db TO postgres;
-- GRANT USAGE ON SCHEMA public TO postgres;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO postgres;
