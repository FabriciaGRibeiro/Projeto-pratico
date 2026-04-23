package com.example.Projeto.pratico.dto;

import com.example.Projeto.pratico.model.Balcao;

import java.time.LocalDateTime;

// DTO de saída — define exatamente o que a API devolve ao cliente.
// Mesmo princípio do BalcaoRequest: isolamos o que é exposto.
// Aqui controlamos que o cliente só vê id, nomeAtendente e criadoEm.
public record BalcaoResponse(
        Long id,
        String nomeAtendente,
        LocalDateTime criadoEm
) {
    // Método estático de fábrica: converte a entidade Balcao → BalcaoResponse.
    // Centraliza a conversão em um único lugar, evitando repetição no service/controller.
    public static BalcaoResponse from(Balcao balcao) {
        return new BalcaoResponse(
                balcao.getId(),
                balcao.getNomeAtendente(),
                balcao.getCriadoEm()
        );
    }
}
