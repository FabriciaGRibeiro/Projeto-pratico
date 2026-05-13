package com.example.Projeto.pratico.exception;

/**
 * Lançada quando um usuário diferente tenta abrir um chamado para um serial
 * que já está EM_ATENDIMENTO por outro usuário.
 *
 * HTTP 403 Forbidden: o servidor entendeu a requisição, mas se recusa a
 * autorizá-la — neste caso, porque o dispositivo já está sendo atendido.
 *
 * Obs: quando o chamado existente estiver CONCLUÍDO, essa restrição não se aplica
 * e um novo chamado pode ser aberto normalmente.
 */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String serialNumber) {
        super(String.format(
                "O dispositivo com serial '%s' já está sendo atendido. " +
                "Aguarde a conclusão do chamado atual.",
                serialNumber
        ));
    }
}