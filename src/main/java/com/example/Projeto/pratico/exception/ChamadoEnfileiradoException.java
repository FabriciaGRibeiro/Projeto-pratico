package com.example.Projeto.pratico.exception;

// Lançada quando todos os balcões estão cheios e o chamado foi colocado
// na fila de espera em vez de salvo no banco.
//
// Isso NÃO é um erro — é um estado válido do sistema. Por isso retorna
// HTTP 202 (Accepted) em vez de 4xx (erro do cliente) ou 5xx (erro do servidor).
// 202 Accepted = "recebi sua requisição e vou processar, mas não agora."
public class ChamadoEnfileiradoException extends RuntimeException {

    public ChamadoEnfileiradoException(String mensagem) {
        super(mensagem);
    }
}
