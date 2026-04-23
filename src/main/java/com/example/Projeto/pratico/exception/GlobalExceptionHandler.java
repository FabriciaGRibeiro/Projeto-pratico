package com.example.Projeto.pratico.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // HTTP 404 — recurso não encontrado
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", ex.getMessage()));
    }

    // HTTP 202 — chamado aceito mas não processado imediatamente (fila de espera).
    // 202 Accepted = "recebemos, vamos processar quando houver espaço."
    @ExceptionHandler(ChamadoEnfileiradoException.class)
    public ResponseEntity<Map<String, String>> handleEnfileirado(ChamadoEnfileiradoException ex) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(Map.of("mensagem", ex.getMessage()));
    }

    // HTTP 400 — campos inválidos na requisição
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.put(erro.getField(), erro.getDefaultMessage());
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("campos", campos));
    }
}
