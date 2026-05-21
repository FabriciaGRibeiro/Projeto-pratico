package com.example.Projeto.pratico.exception;

/**
 * Lançada quando o mesmo usuário tenta abrir um chamado para um serial
 * que já possui um chamado aberto (não concluído) em seu nome.
 *
 * HTTP 409 Conflict: a requisição é válida, mas conflita com o estado atual
 * do recurso no servidor.
 *
 * Carrega o ID do chamado existente para que o handler consiga montar
 * a URL de detalhes no corpo da resposta.
 */
public class ConflitoChamadoException extends RuntimeException {

    private final Long chamadoExistenteId;

    public ConflitoChamadoException(Long chamadoExistenteId, String serialNumber) {
        super(String.format(
                "Já existe um chamado aberto para o serial '%s'. " +
                "Consulte os detalhes em /chamados/%d",
                serialNumber,
                chamadoExistenteId
        ));
        this.chamadoExistenteId = chamadoExistenteId;
    }

    public Long getChamadoExistenteId() {
        return chamadoExistenteId;
    }
}