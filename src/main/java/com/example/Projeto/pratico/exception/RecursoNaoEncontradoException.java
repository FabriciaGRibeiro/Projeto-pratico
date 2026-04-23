package com.example.Projeto.pratico.exception;

// RuntimeException = exceção não verificada.
// Isso significa que não somos obrigados a usar try/catch em todo lugar
// que chamar métodos que podem lançar esse erro. O Spring vai capturar
// automaticamente via @ControllerAdvice.
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
