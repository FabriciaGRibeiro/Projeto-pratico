package com.example.Projeto.pratico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChamadoRequest(

        // @NotNull → rejeita null para tipos não-texto (Long, Integer, etc.)
        // Não usamos @NotBlank aqui porque Long não é uma String
        @NotNull(message = "customer_id é obrigatório")
        Long customerId,

        // Campos opcionais — o chamado pode ser aberto sem essas informações
        String deviceId,
        String serialNumber,

        @NotBlank(message = "motivo é obrigatório")
        String motivo,

        @NotBlank(message = "produto é obrigatório")
        String produto

) {}
