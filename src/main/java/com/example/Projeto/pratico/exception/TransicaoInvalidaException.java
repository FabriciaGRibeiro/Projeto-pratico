package com.example.Projeto.pratico.exception;

import com.example.Projeto.pratico.enums.StatusChamado;

/**
 * Lançada quando se tenta uma transição de status proibida pela máquina de estados.
 *
 * Por que 422 e não 400?
 *   400 = JSON malformado ou campo inválido (problema de sintaxe)
 *   422 = JSON válido, mas a operação não faz sentido no contexto atual (problema semântico)
 *
 * Exemplo: PUT /chamados/1 com status=ABERTO quando o chamado já está CONCLUIDO.
 * O JSON chegou correto, mas a regra de negócio proíbe esse retrocesso.
 */
public class TransicaoInvalidaException extends RuntimeException {

    public TransicaoInvalidaException(StatusChamado atual, StatusChamado destino) {
        super(String.format(
                "Transição inválida: %s → %s. Transições permitidas a partir de '%s': %s",
                atual,
                destino,
                atual,
                atual.transicoesPermitidas().isEmpty()
                        ? "nenhuma (estado terminal)"
                        : atual.transicoesPermitidas()
        ));
    }
}