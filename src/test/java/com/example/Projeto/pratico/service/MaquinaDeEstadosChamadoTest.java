package com.example.Projeto.pratico.service;

import com.example.Projeto.pratico.enums.StatusChamado;
import com.example.Projeto.pratico.exception.TransicaoInvalidaException;
import com.example.Projeto.pratico.model.Balcao;
import com.example.Projeto.pratico.model.Chamado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários da MaquinaDeEstadosChamado.
 *
 * Sem @SpringBootTest, sem mocks — a máquina é lógica pura.
 * Instanciamos direto com "new" para máxima velocidade.
 *
 * Estrutura com @Nested: cada classe representa
 * "dado que o chamado está em estado X, quando tento ir para Y..."
 */
class MaquinaDeEstadosChamadoTest {

    private MaquinaDeEstadosChamado maquina;

    @BeforeEach
    void setUp() {
        maquina = new MaquinaDeEstadosChamado();
    }

    // =========================================================================
    // Helper para criar chamados em estados específicos
    // =========================================================================

    private Chamado chamadoComStatus(StatusChamado status) {
        return Chamado.builder()
                .id(1L)
                .customerId(42L)
                .motivo("Maquininha não liga")
                .produto("Point Pro")
                .status(status)
                .balcao(Balcao.builder().id(1L).nomeAtendente("Ana").build())
                .build();
    }

    // =========================================================================
    // Transições válidas
    // =========================================================================

    @Nested
    @DisplayName("Transições VÁLIDAS")
    class TransicoesValidas {

        @Test
        @DisplayName("ABERTO → EM_ESPERA (fila cheia)")
        void abertoParaEmEspera() {
            Chamado chamado = chamadoComStatus(StatusChamado.ABERTO);
            maquina.transicionar(chamado, StatusChamado.EM_ESPERA);
            assertThat(chamado.getStatus()).isEqualTo(StatusChamado.EM_ESPERA);
        }

        @Test
        @DisplayName("ABERTO → EM_ATENDIMENTO (job inicia atendimento)")
        void abertoParaEmAtendimento() {
            Chamado chamado = chamadoComStatus(StatusChamado.ABERTO);
            maquina.transicionar(chamado, StatusChamado.EM_ATENDIMENTO);
            assertThat(chamado.getStatus()).isEqualTo(StatusChamado.EM_ATENDIMENTO);
        }

        @Test
        @DisplayName("EM_ESPERA → EM_ATENDIMENTO (scheduler promove da fila)")
        void emEsperaParaEmAtendimento() {
            Chamado chamado = chamadoComStatus(StatusChamado.EM_ESPERA);
            maquina.transicionar(chamado, StatusChamado.EM_ATENDIMENTO);
            assertThat(chamado.getStatus()).isEqualTo(StatusChamado.EM_ATENDIMENTO);
        }

        @Test
        @DisplayName("EM_ATENDIMENTO → CONCLUIDO (job finaliza após 2 minutos)")
        void emAtendimentoParaConcluido() {
            Chamado chamado = chamadoComStatus(StatusChamado.EM_ATENDIMENTO);
            maquina.transicionar(chamado, StatusChamado.CONCLUIDO);
            assertThat(chamado.getStatus()).isEqualTo(StatusChamado.CONCLUIDO);
        }
    }

    // =========================================================================
    // Transições inválidas
    // =========================================================================

    @Nested
    @DisplayName("Transições INVÁLIDAS")
    class TransicoesInvalidas {

        @Test
        @DisplayName("ABERTO → CONCLUIDO deve lançar exceção (pula estados obrigatórios)")
        void abertoNaoPodeIrDiretoParaConcluido() {
            Chamado chamado = chamadoComStatus(StatusChamado.ABERTO);
            assertThatThrownBy(() -> maquina.transicionar(chamado, StatusChamado.CONCLUIDO))
                    .isInstanceOf(TransicaoInvalidaException.class)
                    .hasMessageContaining("ABERTO")
                    .hasMessageContaining("CONCLUIDO");
        }

        @Test
        @DisplayName("EM_ESPERA → ABERTO deve lançar exceção (retrocesso proibido)")
        void emEsperaNaoPodeRetrocederParaAberto() {
            Chamado chamado = chamadoComStatus(StatusChamado.EM_ESPERA);
            assertThatThrownBy(() -> maquina.transicionar(chamado, StatusChamado.ABERTO))
                    .isInstanceOf(TransicaoInvalidaException.class);
        }

        @Test
        @DisplayName("EM_ESPERA → CONCLUIDO deve lançar exceção (pula EM_ATENDIMENTO)")
        void emEsperaNaoPodeIrParaConcluido() {
            Chamado chamado = chamadoComStatus(StatusChamado.EM_ESPERA);
            assertThatThrownBy(() -> maquina.transicionar(chamado, StatusChamado.CONCLUIDO))
                    .isInstanceOf(TransicaoInvalidaException.class);
        }

        @Test
        @DisplayName("EM_ATENDIMENTO → ABERTO deve lançar exceção (retrocesso proibido)")
        void emAtendimentoNaoPodeRetrocederParaAberto() {
            Chamado chamado = chamadoComStatus(StatusChamado.EM_ATENDIMENTO);
            assertThatThrownBy(() -> maquina.transicionar(chamado, StatusChamado.ABERTO))
                    .isInstanceOf(TransicaoInvalidaException.class);
        }

        @Test
        @DisplayName("CONCLUIDO → qualquer estado deve lançar exceção (estado terminal)")
        void concluidoEhEstadoTerminal() {
            Chamado chamado = chamadoComStatus(StatusChamado.CONCLUIDO);

            for (StatusChamado destino : StatusChamado.values()) {
                assertThatThrownBy(() -> maquina.transicionar(chamado, destino))
                        .as("CONCLUIDO → %s deveria lançar exceção", destino)
                        .isInstanceOf(TransicaoInvalidaException.class);
            }
        }

        @Test
        @DisplayName("Mesmo estado deve lançar exceção (ABERTO → ABERTO não é transição válida)")
        void mesmoEstadoEhInvalido() {
            Chamado chamado = chamadoComStatus(StatusChamado.ABERTO);
            assertThatThrownBy(() -> maquina.transicionar(chamado, StatusChamado.ABERTO))
                    .isInstanceOf(TransicaoInvalidaException.class);
        }
    }

    // =========================================================================
    // Efeitos colaterais
    // =========================================================================

    @Nested
    @DisplayName("Efeitos colaterais das transições")
    class EfeitosColaterais {

        @Test
        @DisplayName("→ EM_ATENDIMENTO deve preencher dataInicioAtendimento")
        void devePreencherDataInicioAtendimento() {
            Chamado chamado = chamadoComStatus(StatusChamado.ABERTO);
            assertThat(chamado.getDataInicioAtendimento()).isNull();

            maquina.transicionar(chamado, StatusChamado.EM_ATENDIMENTO);

            assertThat(chamado.getDataInicioAtendimento()).isNotNull();
            assertThat(chamado.getDataResolucao()).isNull(); // ainda não concluído
        }

        @Test
        @DisplayName("→ CONCLUIDO deve preencher dataResolucao")
        void devePreencherDataResolucao() {
            Chamado chamado = chamadoComStatus(StatusChamado.EM_ATENDIMENTO);
            assertThat(chamado.getDataResolucao()).isNull();

            maquina.transicionar(chamado, StatusChamado.CONCLUIDO);

            assertThat(chamado.getDataResolucao()).isNotNull();
        }

        @Test
        @DisplayName("→ EM_ESPERA deve limpar dataInicioAtendimento e dataResolucao")
        void deveReiniciarTimestampsParaEmEspera() {
            Chamado chamado = chamadoComStatus(StatusChamado.ABERTO);

            maquina.transicionar(chamado, StatusChamado.EM_ESPERA);

            assertThat(chamado.getDataInicioAtendimento()).isNull();
            assertThat(chamado.getDataResolucao()).isNull();
        }

        @Test
        @DisplayName("dataInicioAtendimento deve ser mantida após CONCLUIDO (para histórico)")
        void deveManterDataInicioAtendimentoAposConcluido() {
            Chamado chamado = chamadoComStatus(StatusChamado.ABERTO);

            maquina.transicionar(chamado, StatusChamado.EM_ATENDIMENTO);
            var inicioAtendimento = chamado.getDataInicioAtendimento();

            maquina.transicionar(chamado, StatusChamado.CONCLUIDO);

            assertThat(chamado.getDataInicioAtendimento())
                    .isEqualTo(inicioAtendimento); // não deve ser apagada
        }
    }
}