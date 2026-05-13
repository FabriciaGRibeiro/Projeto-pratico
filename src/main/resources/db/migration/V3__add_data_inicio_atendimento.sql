-- =============================================================
-- MIGRATION V3 - Adiciona data_inicio_atendimento em tb_chamado
--
-- Por que uma migration nova e não alterar a V2?
--   O Flyway registra o checksum de cada script já executado.
--   Alterar um script já aplicado causa erro na próxima inicialização.
--   Sempre que precisar mudar o banco, crie um novo arquivo Vn__.
-- =============================================================

-- Adiciona a coluna que registra quando o job moveu o chamado para EM_ATENDIMENTO.
-- NULL por padrão: chamados ABERTO e EM_ESPERA ainda não iniciaram atendimento.
ALTER TABLE tb_chamado
    ADD COLUMN data_inicio_atendimento TIMESTAMP NULL;

-- Índice para a query do scheduler:
--   "buscar chamados EM_ATENDIMENTO com data_inicio_atendimento <= agora - 2 min"
-- Sem índice, essa query faria full-scan em toda a tabela a cada 30 segundos.
CREATE INDEX idx_chamado_status_inicio_atendimento
    ON tb_chamado (status, data_inicio_atendimento)
    WHERE status = 'EM_ATENDIMENTO';