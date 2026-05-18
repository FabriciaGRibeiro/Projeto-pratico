package com.example.Projeto.pratico.dto;

import com.example.Projeto.pratico.enums.StatusChamado;
import com.example.Projeto.pratico.model.Chamado;

import java.time.LocalDateTime;

public record ChamadoResponse(
        Long id,
        Long customerId,
        String deviceId,
        String serialNumber,
        String motivo,
        String produto,
        StatusChamado status,
        BalcaoInfo balcao,
        LocalDateTime dataCriacao,
        LocalDateTime dataResolucao,
        LocalDateTime criadoEm,
        ClienteInfo cliente
) {
    // Record aninhado — representa apenas os dados do balcão que fazem sentido
    // aparecer dentro da resposta do chamado. Evita expor tudo de Balcao.
    public record BalcaoInfo(Long id, String nomeAtendente) {}

    public static ChamadoResponse from(Chamado chamado) {
        return from(chamado, null);
    }

    public static ChamadoResponse from(Chamado chamado, ClienteInfo cliente) {
        BalcaoInfo balcaoInfo = null;
        if (chamado.getBalcao() != null) {
            balcaoInfo = new BalcaoInfo(
                    chamado.getBalcao().getId(),
                    chamado.getBalcao().getNomeAtendente()
            );
        }

        return new ChamadoResponse(
                chamado.getId(),
                chamado.getCustomerId(),
                chamado.getDeviceId(),
                chamado.getSerialNumber(),
                chamado.getMotivo(),
                chamado.getProduto(),
                chamado.getStatus(),
                balcaoInfo,
                chamado.getDataCriacao(),
                chamado.getDataResolucao(),
                chamado.getCriadoEm(),
                cliente
        );
    }
}
