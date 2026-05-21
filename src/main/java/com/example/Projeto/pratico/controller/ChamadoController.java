package com.example.Projeto.pratico.controller;

import com.example.Projeto.pratico.dto.ChamadoRequest;
import com.example.Projeto.pratico.dto.ChamadoResponse;
import com.example.Projeto.pratico.dto.ChamadoUpdateRequest;
import com.example.Projeto.pratico.service.ChamadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;

    // POST /chamados → 201 Created
    @PostMapping
    public ResponseEntity<ChamadoResponse> criar(@RequestBody @Valid ChamadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoService.criar(request));
    }

    // GET /chamados → lista paginada de chamados
    // Parâmetros opcionais:
    //   customerId → filtra chamados de um cliente específico
    //   page, size, sort → paginação padrão do Spring (ex: ?page=0&size=10&sort=dataCriacao,desc)
    @GetMapping
    public ResponseEntity<Page<ChamadoResponse>> listarTodos(
            @RequestParam(required = false) Long customerId,
            @PageableDefault(size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        if (customerId != null) {
            return ResponseEntity.ok(chamadoService.listarPorCustomerId(customerId, pageable));
        }
        return ResponseEntity.ok(chamadoService.listarTodos(pageable));
    }

    // GET /chamados/{id} → 200 OK ou 404 se não existir
    @GetMapping("/{id}")
    public ResponseEntity<ChamadoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(chamadoService.buscarPorId(id));
    }

    // PUT /chamados/{id} → 200 com dados atualizados, ou 404
    // Permite atualizar: motivo, produto, deviceId, serialNumber e status.
    // Quando status muda para CONCLUIDO, data_resolucao é preenchida automaticamente.
    @PutMapping("/{id}")
    public ResponseEntity<ChamadoResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ChamadoUpdateRequest request) {
        return ResponseEntity.ok(chamadoService.atualizar(id, request));
    }

    // DELETE /chamados/{id} → 204 No Content ou 404
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        chamadoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
