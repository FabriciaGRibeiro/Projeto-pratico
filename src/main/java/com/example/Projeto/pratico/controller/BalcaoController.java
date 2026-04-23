package com.example.Projeto.pratico.controller;

import com.example.Projeto.pratico.dto.BalcaoRequest;
import com.example.Projeto.pratico.dto.BalcaoResponse;
import com.example.Projeto.pratico.service.BalcaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController = combina @Controller + @ResponseBody.
//   Significa: "essa classe responde requisições HTTP e os métodos
//   retornam JSON automaticamente" (não precisamos converter manualmente).
//
// @RequestMapping("/balcoes") = prefixo de rota para todos os métodos abaixo.
//   Evita repetir "/balcoes" em cada método.
//
// @RequiredArgsConstructor = Lombok injeta o BalcaoService automaticamente.
@RestController
@RequestMapping("/balcoes")
@RequiredArgsConstructor
public class BalcaoController {

    private final BalcaoService balcaoService;

    // POST /balcoes
    // @RequestBody   → lê o corpo JSON da requisição e converte para BalcaoRequest
    // @Valid         → dispara as validações (@NotBlank etc.) do DTO
    // ResponseEntity → nos dá controle total sobre o status HTTP da resposta
    // HttpStatus.CREATED (201) → convenção REST: recurso criado com sucesso
    @PostMapping
    public ResponseEntity<BalcaoResponse> criar(@RequestBody @Valid BalcaoRequest request) {
        BalcaoResponse response = balcaoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /balcoes
    // Retorna 200 OK com a lista de todos os balcões.
    // Se não houver nenhum, retorna 200 com lista vazia [] — não é um erro.
    @GetMapping
    public ResponseEntity<List<BalcaoResponse>> listarTodos() {
        return ResponseEntity.ok(balcaoService.listarTodos());
    }

    // GET /balcoes/{id}
    // @PathVariable → captura o {id} da URL (ex: /balcoes/3 → id = 3)
    // Retorna 200 se encontrado, 404 se não existir (tratado no GlobalExceptionHandler)
    @GetMapping("/{id}")
    public ResponseEntity<BalcaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(balcaoService.buscarPorId(id));
    }

    // PUT /balcoes/{id}
    // Substitui os dados do balcão. Retorna 200 com os dados atualizados.
    // Retorna 404 se o id não existir e 400 se o body for inválido.
    @PutMapping("/{id}")
    public ResponseEntity<BalcaoResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid BalcaoRequest request) {
        return ResponseEntity.ok(balcaoService.atualizar(id, request));
    }

    // DELETE /balcoes/{id}
    // 204 No Content = convenção REST para deleção bem-sucedida.
    // O corpo da resposta é vazio — não há nada para devolver.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        balcaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
