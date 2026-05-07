package com.example.Projeto.pratico.exception;

// Lançada quando se tenta salvar um chamado em um balcão que já atingiu
// a capacidade máxima de chamados ativos. HTTP 409 Conflict.
public class CapacidadeMaximaException extends RuntimeException {

    public CapacidadeMaximaException(String mensagem) {
        super(mensagem);
    }
}
