package com.example.Projeto.pratico.service;

import com.example.Projeto.pratico.enums.StatusChamado;
import com.example.Projeto.pratico.exception.TransicaoInvalidaException;
import com.example.Projeto.pratico.model.Chamado;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/*
 * Máquina de estados do Chamado.
 * Responsabilidade única: executar e validar transições de status.
 */
@Service
public class MaquinaDeEstadosChamado {

    public void validarTransicao(StatusChamado atual, StatusChamado destino) {
        if (!atual.podeTransicionarPara(destino)) {
            throw new TransicaoInvalidaException(atual, destino);
        }
    }

    public Chamado transicionar(Chamado chamado, StatusChamado novoStatus) {
        validarTransicao(chamado.getStatus(), novoStatus);
        chamado.setStatus(novoStatus);
        aplicarEfeitosColaterais(chamado, novoStatus);
        return chamado;
    }

    private void aplicarEfeitosColaterais(Chamado chamado, StatusChamado novoStatus) {
        switch (novoStatus) {
            case EM_ATENDIMENTO -> {
                // Registra quando o atendimento começou.
                // O ProcessamentoChamadoScheduler usa esse timestamp para saber
                // quando os 2 minutos de processamento foram cumpridos.
                chamado.setDataInicioAtendimento(LocalDateTime.now());
                chamado.setDataResolucao(null); 
            }
            case CONCLUIDO -> {
                chamado.setDataResolucao(LocalDateTime.now());
            
            }
            default -> {
                // Para ABERTO ou EM_ESPERA: limpa campos de tempo
                // Previne inconsistências caso o fluxo evolua no futuro
                chamado.setDataInicioAtendimento(null);
                chamado.setDataResolucao(null);
            }
        }
    }
}