package com.example.Projeto.pratico.service;

import com.example.Projeto.pratico.dto.ChamadoRequest;
import com.example.Projeto.pratico.dto.ChamadoResponse;
import com.example.Projeto.pratico.dto.ChamadoUpdateRequest;
import com.example.Projeto.pratico.enums.StatusChamado;
import com.example.Projeto.pratico.exception.AcessoNegadoException;
import com.example.Projeto.pratico.exception.ChamadoEnfileiradoException;
import com.example.Projeto.pratico.exception.ConflitoChamadoException;
import com.example.Projeto.pratico.exception.RecursoNaoEncontradoException;
import com.example.Projeto.pratico.model.Balcao;
import com.example.Projeto.pratico.model.Chamado;
import com.example.Projeto.pratico.repository.BalcaoRepository;
import com.example.Projeto.pratico.repository.ChamadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final BalcaoRepository balcaoRepository;
    private final FilaDeEspera filaDeEspera;
    private final ValidadorChamado validadorChamado;
    private final MaquinaDeEstadosChamado maquinaDeEstados;

    public ChamadoResponse criar(ChamadoRequest request) {
        // 1. Verifica conflitos por serial_number antes de qualquer outra coisa
        verificarConflitos(request);

        if (balcaoRepository.count() == 0) {
            throw new RecursoNaoEncontradoException(
                    "Nenhum balcão cadastrado. Cadastre ao menos um balcão antes de abrir chamados."
            );
        }

        Optional<Balcao> balcaoDisponivel = balcaoRepository.findBalcaoComMenosChamadosAtivos();

        // 2. Todos os balcões cheios → NÃO salva no banco, vai para fila de 3 minutos
        if (balcaoDisponivel.isEmpty()) {
            filaDeEspera.enfileirar(request);
            throw new ChamadoEnfileiradoException(
                    "Todos os balcões estão com capacidade máxima (5/5). " +
                    "Seu chamado foi colocado na fila de espera e será processado automaticamente em até 3 minutos."
            );
        }

        // 3. Há balcão disponível → salva com status ABERTO
        return salvar(request, balcaoDisponivel.get());
    }

    public List<ChamadoResponse> listarTodos() {
        return chamadoRepository.findAll()
                .stream()
                .map(ChamadoResponse::from)
                .toList();
    }

    public ChamadoResponse buscarPorId(Long id) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Chamado não encontrado com id: " + id
                ));
        return ChamadoResponse.from(chamado);
    }

    public ChamadoResponse atualizar(Long id, ChamadoUpdateRequest request) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Chamado não encontrado com id: " + id
                ));

        // Atualiza dados do chamado
        chamado.setMotivo(request.motivo());
        chamado.setProduto(request.produto());
        chamado.setDeviceId(request.deviceId());
        chamado.setSerialNumber(request.serialNumber());

        // Delega mudança de status para a máquina.
        // Se o status não mudou, não executa transição (sem custo e sem erro).
        // Se mudou, a máquina valida e aplica efeitos colaterais (timestamps, etc.)
        if (!chamado.getStatus().equals(request.status())) {
            maquinaDeEstados.transicionar(chamado, request.status());
        }

        return ChamadoResponse.from(chamadoRepository.save(chamado));
    }

    public void deletar(Long id) {
        if (!chamadoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Chamado não encontrado com id: " + id);
        }
        chamadoRepository.deleteById(id);
    }

    /**
     * Salva um chamado com status ABERTO em um balcão específico.
     * Reutilizado pelo ProcessadorDeFilaScheduler ao processar a fila de espera.
     */
    public ChamadoResponse salvar(ChamadoRequest request, Balcao balcao) {
        validadorChamado.validarCapacidade(balcao);

        Chamado chamado = Chamado.builder()
                .customerId(request.customerId())
                .deviceId(request.deviceId())
                .serialNumber(request.serialNumber())
                .motivo(request.motivo())
                .produto(request.produto())
                .balcao(balcao)
                .build(); // status padrão = ABERTO (definido no @Builder.Default do model)

        Chamado salvo = chamadoRepository.save(chamado);
        return ChamadoResponse.from(chamadoRepository.findById(salvo.getId()).orElseThrow());
    }

    // -------------------------------------------------------------------------
    // Privado
    // -------------------------------------------------------------------------

    /**
     * Verifica regras de conflito por serial_number antes de criar um chamado.
     *
     * Regra 1 — mesmo usuário, mesmo serial, chamado aberto → 409 Conflict
     *   O usuário já tem um chamado em andamento para esse dispositivo.
     *   A resposta inclui a URL do chamado existente.
     *
     * Regra 2 — usuário diferente, mesmo serial, status EM_ATENDIMENTO → 403 Forbidden
     *   O dispositivo está sendo atendido para outro cliente.
     *   Quando concluído, qualquer usuário pode abrir um novo chamado.
     *
     * Se o chamado existente estiver CONCLUIDO, nenhuma restrição se aplica.
     * (A query findChamadoAbertoBySerialNumber já filtra CONCLUIDO fora.)
     */
    private void verificarConflitos(ChamadoRequest request) {
        if (request.serialNumber() == null || request.serialNumber().isBlank()) {
            return; // sem serial = sem conflito possível
        }

        Optional<Chamado> existente = chamadoRepository
                .findChamadoAbertoBySerialNumber(request.serialNumber());

        if (existente.isEmpty()) {
            return; // nenhum chamado aberto para esse serial
        }

        Chamado chamadoAberto = existente.get();

        // Regra 1: mesmo usuário
        if (chamadoAberto.getCustomerId().equals(request.customerId())) {
            throw new ConflitoChamadoException(chamadoAberto.getId(), request.serialNumber());
        }

        // Regra 2: usuário diferente + serial EM_ATENDIMENTO
        if (chamadoAberto.getStatus() == StatusChamado.EM_ATENDIMENTO) {
            throw new AcessoNegadoException(request.serialNumber());
        }

        // Usuário diferente + serial ABERTO ou EM_ESPERA → pode abrir (sem restrição explícita)
    }
}