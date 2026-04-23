-- =============================================================
-- MIGRATION V2 - Criação das tabelas: balcao, atendente, fila e chamado
--
-- O que é uma migration?
-- É um arquivo SQL versionado que o Flyway executa na ordem certa
-- sempre que a aplicação sobe. Assim todo mundo no time (e o banco
-- em produção) tem exatamente o mesmo estado.
-- =============================================================


-- -------------------------------------------------------------
-- TABELA: tb_balcao
-- Representa um balcão de atendimento físico.
--
-- BIGSERIAL = número inteiro que se incrementa automaticamente
--             (1, 2, 3...). Não precisa informar na hora de inserir.
-- PRIMARY KEY = identificador único de cada linha
-- NOT NULL = campo obrigatório, não aceita vazio
-- DEFAULT NOW() = se não informar a data, usa a data/hora atual
-- -------------------------------------------------------------
CREATE TABLE tb_balcao (
    id             BIGSERIAL    PRIMARY KEY,
    nome_atendente VARCHAR(255) NOT NULL,
    criado_em      TIMESTAMP    NOT NULL DEFAULT NOW()
);


-- -------------------------------------------------------------
-- TABELA: tb_atendente
-- Representa um atendente vinculado a um balcão.
--
-- REFERENCES tb_balcao(id) = chave estrangeira.
--   Isso significa: "o valor de balcao_id DEVE existir na
--   coluna id da tabela tb_balcao". O banco rejeita valores
--   inválidos automaticamente.
-- -------------------------------------------------------------
CREATE TABLE tb_atendente (
    id          BIGSERIAL    PRIMARY KEY,
    nome        VARCHAR(255) NOT NULL,
    matricula   VARCHAR(50)  NOT NULL UNIQUE,
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    balcao_id   BIGINT       REFERENCES tb_balcao(id),
    criado_em   TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP  NOT NULL DEFAULT NOW()
);


-- -------------------------------------------------------------
-- TABELA: tb_fila
-- Representa uma fila de espera dentro de um balcão.
-- -------------------------------------------------------------
CREATE TABLE tb_fila (
    id            BIGSERIAL   PRIMARY KEY,
    nome          VARCHAR(255) NOT NULL,
    sigla         VARCHAR(10)  NOT NULL,
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    balcao_id     BIGINT       REFERENCES tb_balcao(id),
    criado_em     TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP    NOT NULL DEFAULT NOW()
);


-- -------------------------------------------------------------
-- TABELA: tb_chamado
-- Representa um chamado de atendimento aberto por um cliente.
--
-- status com CHECK CONSTRAINT:
--   Funciona como um enum no banco de dados. O banco rejeita
--   qualquer valor que não esteja na lista. Aqui usamos VARCHAR
--   (texto) com uma regra de validação, o que funciona perfeitamente
--   com a anotação @Enumerated(EnumType.STRING) do Java/Hibernate.
--
-- balcao_id REFERENCES tb_balcao(id):
--   Relacionamento entre chamado e balcão. Um chamado pode (ou não)
--   estar vinculado a um balcão. Por isso não é NOT NULL — o chamado
--   pode existir sem balcão ainda (ex: status ABERTO, aguardando
--   ser direcionado).
--
-- data_resolucao:
--   Pode ser NULL porque o chamado só tem data de resolução quando
--   for concluído. No início, fica vazio.
-- -------------------------------------------------------------
CREATE TABLE tb_chamado (
    id              BIGSERIAL    PRIMARY KEY,
    customer_id     BIGINT       NOT NULL,
    device_id       VARCHAR(255),
    serial_number   VARCHAR(255),
    motivo          VARCHAR(500) NOT NULL,
    produto         VARCHAR(255) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ABERTO'
                        CHECK (status IN ('ABERTO', 'EM_ESPERA', 'EM_ATENDIMENTO', 'CONCLUIDO')),
    balcao_id       BIGINT       REFERENCES tb_balcao(id),
    data_criacao    TIMESTAMP    NOT NULL DEFAULT NOW(),
    data_resolucao  TIMESTAMP,
    criado_em       TIMESTAMP    NOT NULL DEFAULT NOW()
);
