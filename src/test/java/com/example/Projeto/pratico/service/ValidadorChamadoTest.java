package com.example.Projeto.pratico.service;

import com.example.Projeto.pratico.enums.StatusChamado;
import com.example.Projeto.pratico.exception.CapacidadeMaximaException;
import com.example.Projeto.pratico.model.Balcao;
import com.example.Projeto.pratico.repository.ChamadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidadorChamadoTest {

    @Mock
    private ChamadoRepository chamadoRepository;

    @InjectMocks
    private ValidadorChamado validadorChamado;

    private Balcao balcao;

    @BeforeEach
    void setUp() {
        balcao = Balcao.builder()
                .id(1L)
                .nomeAtendente("Ana")
                .build();
    }

    @Test
    void devePassarQuandoBalcaoTemMenosDeCincoChamadosAtivos() {
        when(chamadoRepository.countByBalcaoIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(4L);

        assertThatCode(() -> validadorChamado.validarCapacidade(balcao))
                .doesNotThrowAnyException();
    }

    @Test
    void deveLancarExcecaoQuandoBalcaoAtingeCapacidadeMaxima() {
        when(chamadoRepository.countByBalcaoIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(5L);

        assertThatThrownBy(() -> validadorChamado.validarCapacidade(balcao))
                .isInstanceOf(CapacidadeMaximaException.class)
                .hasMessageContaining("Ana")
                .hasMessageContaining("5");
    }

    @Test
    void deveLancarExcecaoQuandoBalcaoUltrapassaCapacidadeMaxima() {
        when(chamadoRepository.countByBalcaoIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(6L);

        assertThatThrownBy(() -> validadorChamado.validarCapacidade(balcao))
                .isInstanceOf(CapacidadeMaximaException.class);
    }

    @Test
    void devePassarQuandoBalcaoEstaVazio() {
        when(chamadoRepository.countByBalcaoIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(0L);

        assertThatCode(() -> validadorChamado.validarCapacidade(balcao))
                .doesNotThrowAnyException();
    }
}
