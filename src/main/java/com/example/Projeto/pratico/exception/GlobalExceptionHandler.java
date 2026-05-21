package com.example.Projeto.pratico.exception;

import com.example.Projeto.pratico.dto.ErroResponse;
import com.example.Projeto.pratico.dto.ErroValidacaoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // HTTP 404 — recurso não encontrado
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErroResponse(ex.getMessage()));
    }

    // HTTP 409 — mesmo usuário tentando abrir chamado para serial já em aberto.
    //
    // Location header: padrão REST para indicar onde o recurso conflitante está.
    // Além da mensagem no body, o cliente recebe o header "Location: /chamados/{id}"
    // para redirecionar ou linkar diretamente ao chamado existente.
    @ExceptionHandler(ConflitoChamadoException.class)
    public ResponseEntity<ErroResponse> handleConflito(ConflitoChamadoException ex) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/chamados/{id}")
                .buildAndExpand(ex.getChamadoExistenteId())
                .toUri();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .location(location)
                .body(new ErroResponse(ex.getMessage()));
    }

    // HTTP 409 — balcão com capacidade máxima atingida (ValidadorChamado)
    @ExceptionHandler(CapacidadeMaximaException.class)
    public ResponseEntity<ErroResponse> handleCapacidadeMaxima(CapacidadeMaximaException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErroResponse(ex.getMessage()));
    }

    // HTTP 403 — dispositivo em atendimento por outro usuário
    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponse> handleAcessoNegado(AcessoNegadoException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErroResponse(ex.getMessage()));
    }

    // HTTP 202 — chamado aceito mas na fila de espera (não processado imediatamente)
    @ExceptionHandler(ChamadoEnfileiradoException.class)
    public ResponseEntity<ErroResponse> handleEnfileirado(ChamadoEnfileiradoException ex) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new ErroResponse(ex.getMessage()));
    }

    // HTTP 422 — transição de status inválida pela máquina de estados.
    //
    // 400 = JSON malformado ou campo inválido (sintaxe)
    // 422 = JSON válido, mas a operação é semanticamente proibida no contexto atual
    @ExceptionHandler(TransicaoInvalidaException.class)
    public ResponseEntity<ErroResponse> handleTransicaoInvalida(TransicaoInvalidaException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroResponse(ex.getMessage()));
    }

    // HTTP 400 — campos inválidos na requisição (@Valid falhou)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroValidacaoResponse> handleValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.put(erro.getField(), erro.getDefaultMessage());
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErroValidacaoResponse(campos));
    }
}