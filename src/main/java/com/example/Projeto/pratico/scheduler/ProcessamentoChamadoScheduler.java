package com.example.Projeto.pratico.scheduler;

import com.example.Projeto.pratico.enums.StatusChamado;
import com.example.Projeto.pratico.model.Balcao;
import com.example.Projeto.pratico.model.Chamado;
import com.example.Projeto.pratico.queue.ChamadoEmEspera;
import com.example.Projeto.pratico.repository.BalcaoRepository;
import com.example.Projeto.pratico.repository.ChamadoRepository;
import com.example.Projeto.pratico.service.ChamadoService;
import com.example.Projeto.pratico.service.FilaDeEspera;
import com.example.Projeto.pratico.service.MaquinaDeEstadosChamado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Job responsável por avançar o ciclo de vida dos chamados.
 *
 * Executa a cada 30 segundos e realiza dois passos na ordem:
 *
 * Passo 1 — ABERTO → EM_ATENDIMENTO
 *   Busca chamados com status ABERTO e os move para EM_ATENDIMENTO.
 *   A máquina de estados registra dataInicioAtendimento automaticamente.
 *
 * Passo 2 — EM_ATENDIMENTO → CONCLUÍDO (após 2 minutos)
 *   Busca chamados EM_ATENDIMENTO cujos 2 minutos já expiraram
 *   (dataInicioAtendimento <= agora - 2 min) e os conclui.
 *   A máquina registra dataResolucao automaticamente.
 *
 * Por que o Passo 2 vem depois do Passo 1 e não antes?
 *   Se invertêssemos, um chamado recém-movido para EM_ATENDIMENTO
 *   (dataInicioAtendimento = agora) nunca seria concluído na mesma execução,
 *   mas queremos garantir que um chamado ABERTO não seja concluído antes de
 *   completar os 2 minutos. Essa ordem torna isso impossível.
 *
 * Por que @Transactional aqui?
 *   O @Transactional garante que todas as operações desta execução façam
 *   parte de uma única transação. Se qualquer save() falhar, o banco
 *   reverte tudo — evitando estados inconsistentes (ex: status atualizado
 *   mas dataResolucao não gravada).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessamentoChamadoScheduler {

    private static final long TEMPO_ATENDIMENTO_MINUTOS = 2;

    private final ChamadoRepository chamadoRepository;
    private final MaquinaDeEstadosChamado maquinaDeEstados;
    private final FilaDeEspera filaDeEspera;
    private final BalcaoRepository balcaoRepository;
    private final ChamadoService chamadoService;

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void processarChamados() {

        // -----------------------------------------------------------------
        // Passo 1: ABERTO → EM_ATENDIMENTO
        // -----------------------------------------------------------------
        List<Chamado> chamadosAbertos = chamadoRepository.findByStatus(StatusChamado.ABERTO);

        if (!chamadosAbertos.isEmpty()) {
            log.info("[ProcessamentoChamado] Iniciando atendimento de {} chamado(s) ABERTO(s)",
                    chamadosAbertos.size());
        }

        for (Chamado chamado : chamadosAbertos) {
            // transicionar() valida a transição e seta dataInicioAtendimento
            maquinaDeEstados.transicionar(chamado, StatusChamado.EM_ATENDIMENTO);
            chamadoRepository.save(chamado);

            log.info("[ProcessamentoChamado] Chamado #{} → EM_ATENDIMENTO (balcão: {})",
                    chamado.getId(),
                    chamado.getBalcao() != null ? chamado.getBalcao().getNomeAtendente() : "N/A");
        }

        // -----------------------------------------------------------------
        // Passo 2: EM_ATENDIMENTO → CONCLUÍDO (timer de 2 minutos)
        // -----------------------------------------------------------------

        // "Há quanto tempo iniciou?" → dataInicioAtendimento <= agora - 2 min
        // Se dataInicioAtendimento é 10:00 e agora é 10:02, o limite é 10:00 → condição satisfeita
        LocalDateTime limite = LocalDateTime.now().minusMinutes(TEMPO_ATENDIMENTO_MINUTOS);

        List<Chamado> prontosConcluir = chamadoRepository
                .findByStatusAndDataInicioAtendimentoBefore(StatusChamado.EM_ATENDIMENTO, limite);

        if (!prontosConcluir.isEmpty()) {
            log.info("[ProcessamentoChamado] Concluindo {} chamado(s) que completaram {} minuto(s)",
                    prontosConcluir.size(), TEMPO_ATENDIMENTO_MINUTOS);
        }

        for (Chamado chamado : prontosConcluir) {
            // transicionar() valida a transição e seta dataResolucao
            maquinaDeEstados.transicionar(chamado, StatusChamado.CONCLUIDO);
            chamadoRepository.save(chamado);

            log.info("[ProcessamentoChamado] Chamado #{} → CONCLUÍDO (resolução: {})",
                    chamado.getId(), chamado.getDataResolucao());
        }

        // -----------------------------------------------------------------
        // Passo 3: Drena a FilaDeEspera — processa chamados aguardando vaga
        // -----------------------------------------------------------------
        ChamadoEmEspera emEspera;
        while ((emEspera = filaDeEspera.poll()) != null) {
            Optional<Balcao> balcaoDisponivel = balcaoRepository.findBalcaoComMenosChamadosAtivos();
            if (balcaoDisponivel.isPresent()) {
                chamadoService.salvar(emEspera.getRequest(), balcaoDisponivel.get());
                log.info("[ProcessamentoChamado] Chamado da fila de espera alocado no balcão '{}'",
                        balcaoDisponivel.get().getNomeAtendente());
            } else {
                // Ainda não há vaga: recoloca na fila e para de drenar nessa execução
                filaDeEspera.enfileirar(emEspera.getRequest());
                log.info("[ProcessamentoChamado] Fila de espera: todos os balcões ainda cheios, reagendando.");
                break;
            }
        }
    }
}