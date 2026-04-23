package com.example.Projeto.pratico.service;

import com.example.Projeto.pratico.dto.ChamadoRequest;
import com.example.Projeto.pratico.dto.ChamadoResponse;
import com.example.Projeto.pratico.dto.ChamadoUpdateRequest;
import com.example.Projeto.pratico.enums.StatusChamado;
import com.example.Projeto.pratico.exception.ChamadoEnfileiradoException;
import com.example.Projeto.pratico.exception.RecursoNaoEncontradoException;
import com.example.Projeto.pratico.model.Balcao;
import com.example.Projeto.pratico.model.Chamado;
import com.example.Projeto.pratico.repository.BalcaoRepository;
import com.example.Projeto.pratico.repository.ChamadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final BalcaoRepository balcaoRepository;
    private final FilaDeEspera filaDeEspera;

    public ChamadoResponse criar(ChamadoRequest request) {
        if (balcaoRepository.count() == 0) {
            throw new RecursoNaoEncontradoException(
                    "Nenhum balcão cadastrado. Cadastre ao menos um balcão antes de abrir chamados."
            );
        }

        Optional<Balcao> balcaoDisponivel = balcaoRepository.findBalcaoComMenosChamadosAtivos();

        if (balcaoDisponivel.isEmpty()) {
            filaDeEspera.enfileirar(request);
            throw new ChamadoEnfileiradoException(
                    "Todos os balcões estão com capacidade máxima (5/5). " +
                    "Seu chamado foi colocado na fila de espera e será processado automaticamente em até 3 minutos."
            );
        }

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

        chamado.setMotivo(request.motivo());
        chamado.setProduto(request.produto());
        chamado.setDeviceId(request.deviceId());
        chamado.setSerialNumber(request.serialNumber());
        chamado.setStatus(request.status());

        // Quando o chamado é concluído, registramos automaticamente a data de resolução.
        // Se o status voltar de CONCLUIDO para outro, limpamos a data.
        if (request.status() == StatusChamado.CONCLUIDO) {
            chamado.setDataResolucao(LocalDateTime.now());
        } else {
            chamado.setDataResolucao(null);
        }

        return ChamadoResponse.from(chamadoRepository.save(chamado));
    }

    public void deletar(Long id) {
        if (!chamadoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Chamado não encontrado com id: " + id);
        }
        chamadoRepository.deleteById(id);
    }

    // Reutilizado pelo scheduler ao salvar chamados da fila de espera
    public ChamadoResponse salvar(ChamadoRequest request, Balcao balcao) {
        Chamado chamado = Chamado.builder()
                .customerId(request.customerId())
                .deviceId(request.deviceId())
                .serialNumber(request.serialNumber())
                .motivo(request.motivo())
                .produto(request.produto())
                .balcao(balcao)
                .build();

        Chamado salvo = chamadoRepository.save(chamado);
        return ChamadoResponse.from(chamadoRepository.findById(salvo.getId()).orElseThrow());
    }
}
