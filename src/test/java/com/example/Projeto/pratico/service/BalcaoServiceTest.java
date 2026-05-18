package com.example.Projeto.pratico.service;

import com.example.Projeto.pratico.dto.BalcaoRequest;
import com.example.Projeto.pratico.dto.BalcaoResponse;
import com.example.Projeto.pratico.exception.RecursoNaoEncontradoException;
import com.example.Projeto.pratico.model.Balcao;
import com.example.Projeto.pratico.repository.BalcaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalcaoServiceTest {

    @Mock
    private BalcaoRepository balcaoRepository;

    @InjectMocks
    private BalcaoService balcaoService;

    private Balcao balcao;

    @BeforeEach
    void setUp() {
        balcao = Balcao.builder()
                .id(1L)
                .nomeAtendente("Ana")
                .build();
    }

    @Nested
    @DisplayName("criar()")
    class Criar {

        @Test
        @DisplayName("deve salvar e retornar o balcão criado")
        void deveCriarBalcao() {
            when(balcaoRepository.save(any(Balcao.class))).thenReturn(balcao);

            BalcaoResponse response = balcaoService.criar(new BalcaoRequest("Ana"));

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.nomeAtendente()).isEqualTo("Ana");
            verify(balcaoRepository).save(any(Balcao.class));
        }
    }

    @Nested
    @DisplayName("listarTodos()")
    class ListarTodos {

        @Test
        @DisplayName("deve retornar todos os balcões mapeados para DTO")
        void deveListarTodos() {
            when(balcaoRepository.findAll()).thenReturn(List.of(balcao));

            List<BalcaoResponse> result = balcaoService.listarTodos();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).nomeAtendente()).isEqualTo("Ana");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há balcões")
        void deveRetornarListaVazia() {
            when(balcaoRepository.findAll()).thenReturn(List.of());

            assertThat(balcaoService.listarTodos()).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar o balcão quando encontrado")
        void deveBuscarPorId() {
            when(balcaoRepository.findById(1L)).thenReturn(Optional.of(balcao));

            BalcaoResponse response = balcaoService.buscarPorId(1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.nomeAtendente()).isEqualTo("Ana");
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando id não existir")
        void deveLancarExcecaoQuandoNaoEncontrar() {
            when(balcaoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> balcaoService.buscarPorId(99L))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar o nome do atendente e retornar o balcão atualizado")
        void deveAtualizarBalcao() {
            Balcao atualizado = Balcao.builder().id(1L).nomeAtendente("Carlos").build();
            when(balcaoRepository.findById(1L)).thenReturn(Optional.of(balcao));
            when(balcaoRepository.save(any(Balcao.class))).thenReturn(atualizado);

            BalcaoResponse response = balcaoService.atualizar(1L, new BalcaoRequest("Carlos"));

            assertThat(response.nomeAtendente()).isEqualTo("Carlos");
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando id não existir")
        void deveLancarExcecaoQuandoNaoEncontrar() {
            when(balcaoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> balcaoService.atualizar(99L, new BalcaoRequest("X")))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("deletar()")
    class Deletar {

        @Test
        @DisplayName("deve deletar o balcão sem lançar exceção")
        void deveDeletarBalcao() {
            when(balcaoRepository.existsById(1L)).thenReturn(true);

            assertThatCode(() -> balcaoService.deletar(1L)).doesNotThrowAnyException();
            verify(balcaoRepository).deleteById(1L);
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando id não existir")
        void deveLancarExcecaoQuandoNaoEncontrar() {
            when(balcaoRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> balcaoService.deletar(99L))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("99");
        }
    }
}
