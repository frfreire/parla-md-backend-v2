-- =========================================================================
-- PARLA-MD - SCRIPT DE INICIALIZACAO DO POSTGRESQL (KEYCLOAK)
-- =========================================================================
-- Este script configura o banco de dados PostgreSQL para o Keycloak
-- =========================================================================

\echo 'Iniciando configuracao do banco Keycloak...'

-- Configuracoes de performance para o Keycloak
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET max_connections = 200;
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;
ALTER SYSTEM SET random_page_cost = 1.1;
ALTER SYSTEM SET effective_io_concurrency = 200;

-- Criar extensoes uteis se disponiveis
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Garantir que o banco keycloak esta criado e configurado
-- (As tabelas serao criadas pelo Keycloak na primeira inicializacao)

-- Configuracoes de encoding para suporte completo UTF-8
UPDATE pg_database SET datcollate='C', datctype='C' WHERE datname='keycloak';

-- Conceder todas as permissoes necessarias
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
GRANT ALL PRIVILEGES ON SCHEMA public TO keycloak;
GRANT CREATE ON SCHEMA public TO keycloak;

-- Configurar search_path
ALTER USER keycloak SET search_path TO public;

\echo 'Configuracao do banco Keycloak concluida! O Keycloak criara as tabelas na primeira inicializacao.'
