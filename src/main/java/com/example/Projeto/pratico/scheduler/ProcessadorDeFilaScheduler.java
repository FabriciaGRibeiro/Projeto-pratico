package com.example.Projeto.pratico.scheduler;

import com.example.Projeto.pratico.model.Balcao;
import com.example.Projeto.pratico.queue.ChamadoEmEspera;
import com.example.Projeto.pratico.repository.BalcaoRepository;
import com.example.Projeto.pratico.service.ChamadoService;
import com.example.Projeto.pratico.service.FilaDeEspera;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

// @Slf4j (Lombok) → gera automaticamente um logger chamado "log".
// Usamos log.info() para acompanhar o que o scheduler está fazendo
// sem precisar de System.out.println.
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessadorDeFilaScheduler {

    private final FilaDeEspera filaDeEspera;
    private final BalcaoRepository balcaoRepository;
    private final ChamadoService chamadoService;

    // @Scheduled(fixedDelay = 30_000) → roda a cada 30 segundos,
    // APÓS a execução anterior terminar (não em paralelo).
    //
    // Por que 30 segundos e não 3 minutos?
    //   O delay de 3 minutos está no ChamadoEmEspera (ele só fica disponível
    //   depois de 3 min). O scheduler pode verificar com mais frequência —
    //   o DelayQueue.poll() retorna null se nenhum elemento estiver pronto,
    //   então verificar de 30 em 30 segundos não tem custo relevante.
    //
    // @Transactional → garante que o save() no banco seja atômico.
    // Se algo falhar no meio, o banco volta ao estado anterior.
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void processarFila() {
        if (filaDeEspera.tamanho() == 0) {
            return; // fila vazia, nada a fazer
        }

        log.info("[FilaDeEspera] Verificando fila. Chamados aguardando: {}", filaDeEspera.tamanho());

        // Consome todos os elementos PRONTOS (delay vencido) da fila de uma vez.
        // poll() retorna null quando não há mais elementos prontos — sai do loop.
        ChamadoEmEspera item;
        while ((item = filaDeEspera.poll()) != null) {
            Optional<Balcao> balcaoDisponivel = balcaoRepository.findBalcaoComMenosChamadosAtivos();

            if (balcaoDisponivel.isPresent()) {
                // Há espaço — salva o chamado no banco com status ABERTO
                chamadoService.salvar(item.getRequest(), balcaoDisponivel.get());
                log.info("[FilaDeEspera] Chamado salvo no balcão '{}'",
                        balcaoDisponivel.get().getNomeAtendente());
            } else {
                // Ainda cheio — re-enfileira por mais 3 minutos e continua tentando
                filaDeEspera.enfileirar(item.getRequest());
                log.info("[FilaDeEspera] Todos os balcões ainda cheios. Chamado reenfileirado por +3 minutos.");
            }
        }
    }
}
