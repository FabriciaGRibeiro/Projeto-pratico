package com.example.Projeto.pratico.dto;

import com.example.Projeto.pratico.enums.StatusChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// DTO exclusivo para atualização de chamado.
// Separado do ChamadoRequest porque aqui o cliente pode mudar o status —
// algo que não faz sentido na criação (sempre começa como ABERTO).
public record ChamadoUpdateRequest(

        @NotBlank(message = "motivo é obrigatório")
        String motivo,

        @NotBlank(message = "produto é obrigatório")
        String produto,

        String deviceId,
        String serialNumber,

        // @NotNull → o status deve ser informado no update.
        // O valor deve ser um dos definidos no enum: ABERTO, EM_ESPERA, EM_ATENDIMENTO, CONCLUIDO.
        @NotNull(message = "status é obrigatório")
        StatusChamado status

) {}
