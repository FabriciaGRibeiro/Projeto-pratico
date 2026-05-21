package com.example.Projeto.pratico.service;

import com.example.Projeto.pratico.enums.StatusChamado;
import com.example.Projeto.pratico.exception.CapacidadeMaximaException;
import com.example.Projeto.pratico.model.Balcao;
import com.example.Projeto.pratico.repository.ChamadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidadorChamado {

    static final int CAPACIDADE_MAXIMA = 5;

    // Status considerados "ativos" para fins de contagem de capacidade
    private static final List<StatusChamado> STATUS_ATIVOS = List.of(
            StatusChamado.ABERTO,
            StatusChamado.EM_ESPERA,
            StatusChamado.EM_ATENDIMENTO
    );

    private final ChamadoRepository chamadoRepository;

    /**
     * Verifica se o balcão ainda tem capacidade para receber um novo chamado.
     * Lança CapacidadeMaximaException se o limite de {@value CAPACIDADE_MAXIMA} chamados ativos for atingido.
     */
    public void validarCapacidade(Balcao balcao) {
        long chamadosAtivos = chamadoRepository.countByBalcaoIdAndStatusIn(balcao.getId(), STATUS_ATIVOS);

        if (chamadosAtivos >= CAPACIDADE_MAXIMA) {
            throw new CapacidadeMaximaException(
                    "Balcão '" + balcao.getNomeAtendente() + "' atingiu a capacidade máxima de "
                    + CAPACIDADE_MAXIMA + " chamados ativos."
            );
        }
    }
}
