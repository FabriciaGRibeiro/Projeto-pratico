package com.example.Projeto.pratico.dto;

import jakarta.validation.constraints.NotBlank;

// DTO (Data Transfer Object) de entrada — representa o corpo do JSON que o cliente envia.
//
// Por que usar um DTO em vez de receber a entidade diretamente?
//   A entidade (Balcao.java) é o espelho exato do banco. Se expusermos ela
//   diretamente na API, o cliente poderia tentar enviar o campo "id" ou
//   "criadoEm", que ele não deve controlar. O DTO filtra exatamente o que
//   o cliente pode informar.
//
// record = tipo imutável do Java 16+. Já gera construtor, getters,
//          equals, hashCode e toString automaticamente. Perfeito para DTOs.
public record BalcaoRequest(

        // @NotBlank → rejeita null, "", "   " (string vazia ou só espaços)
        // O message aparece no corpo do erro 400 quando a validação falhar.
        @NotBlank(message = "nome_atendente é obrigatório")
        String nomeAtendente

) {}
