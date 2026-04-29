-- =============================================================
-- MIGRATION V3 - Criação da tabela de usuários para autenticação
--
-- Esta tabela guarda os usuários que podem se autenticar na API.
-- A senha é sempre armazenada como hash BCrypt, NUNCA em texto puro.
-- =============================================================

CREATE TABLE tb_usuario (
    id       BIGSERIAL    PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50)  NOT NULL DEFAULT 'USER'
);
