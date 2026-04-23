package com.example.Projeto.pratico.queue;

import com.example.Projeto.pratico.dto.ChamadoRequest;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

// ChamadoEmEspera representa um chamado que está aguardando na fila.
// Ele carrega o request original e sabe quando deve ser processado.
//
// Por que implementar Delayed?
//   A interface Delayed é o contrato que o DelayQueue exige dos seus elementos.
//   Ela define o método getDelay() — que diz quanto tempo ainda falta para
//   o elemento ficar disponível — e compareTo() — para ordenar a fila.
//
//   Funciona como uma fila de prioridade por tempo:
//   o DelayQueue só entrega o elemento quando getDelay() retornar <= 0.
public class ChamadoEmEspera implements Delayed {

    private static final long DELAY_MILLIS = 3 * 60 * 1_000L; // 3 minutos

    private final ChamadoRequest request;

    // Armazenamos o momento exato em que o chamado ficará disponível para processamento.
    // System.currentTimeMillis() = tempo atual em ms desde 01/01/1970 (Unix epoch)
    private final long disponivelEm;

    public ChamadoEmEspera(ChamadoRequest request) {
        this.request = request;
        this.disponivelEm = System.currentTimeMillis() + DELAY_MILLIS;
    }

    public ChamadoRequest getRequest() {
        return request;
    }

    // getDelay() → quanto tempo AINDA FALTA para o elemento estar disponível.
    // Quando retornar <= 0, o DelayQueue libera o elemento para consumo.
    // O unit define a unidade que quem está perguntando quer receber (ms, s, etc.)
    @Override
    public long getDelay(TimeUnit unit) {
        long tempoRestante = disponivelEm - System.currentTimeMillis();
        return unit.convert(tempoRestante, TimeUnit.MILLISECONDS);
    }

    // compareTo() → define a ordem dos elementos na fila.
    // O elemento com menor tempo de espera restante fica na frente.
    @Override
    public int compareTo(Delayed outro) {
        return Long.compare(
                this.getDelay(TimeUnit.MILLISECONDS),
                outro.getDelay(TimeUnit.MILLISECONDS)
        );
    }
}
