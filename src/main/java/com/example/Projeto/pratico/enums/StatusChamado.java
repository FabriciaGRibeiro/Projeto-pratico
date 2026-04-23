package com.example.Projeto.pratico.enums;

/**
 * Representa os possíveis estados de um chamado durante seu ciclo de vida.
 *
 * ABERTO          → chamado foi criado, ainda não entrou na fila
 * EM_ESPERA       → chamado na fila aguardando um balcão ficar disponível
 * EM_ATENDIMENTO  → chamado sendo atendido por um atendente em um balcão
 * CONCLUIDO       → chamado finalizado com sucesso
 */
public enum StatusChamado {
    ABERTO,
    EM_ESPERA,
    EM_ATENDIMENTO,
    CONCLUIDO
}
