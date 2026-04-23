package com.example.Projeto.pratico.controller;

import com.example.Projeto.pratico.dto.ChamadoRequest;
import com.example.Projeto.pratico.dto.ChamadoResponse;
import com.example.Projeto.pratico.dto.ChamadoUpdateRequest;
import com.example.Projeto.pratico.service.ChamadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // GET /chamados → 200 OK com lista de todos os chamados
    @GetMapping
    public ResponseEntity<List<ChamadoResponse>> listarTodos() {
        return ResponseEntity.ok(chamadoService.listarTodos());
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
