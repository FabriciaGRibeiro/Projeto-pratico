package com.example.Projeto.pratico.service;

import com.example.Projeto.pratico.dto.ChamadoRequest;
import com.example.Projeto.pratico.queue.ChamadoEmEspera;
import org.springframework.stereotype.Component;

import java.util.concurrent.DelayQueue;

// FilaDeEspera é a fila em memória para chamados que não puderam ser salvos
// porque todos os balcões estavam cheios.
//
// Por que @Component e não @Service?
//   @Service é semântico — usamos para classes com lógica de negócio.
//   @Component é mais genérico — aqui estamos apenas gerenciando uma estrutura
//   de dados em memória, sem lógica de negócio. Ambos funcionariam igual para
//   o Spring, mas @Component comunica melhor a intenção.
//
// Por que DelayQueue?
//   É uma fila especial do Java onde os elementos só ficam disponíveis para
//   consumo depois que o delay deles vence. Perfeito para o nosso caso:
//   o chamado entra com delay de 3 minutos e só sai quando o tempo passar.
//
// Por que é thread-safe?
//   DelayQueue é thread-safe internamente. Quando o scheduler tentar consumir
//   e o controller tentar enfileirar ao mesmo tempo, não haverá conflito.
@Component
public class FilaDeEspera {

    private final DelayQueue<ChamadoEmEspera> fila = new DelayQueue<>();

    // Adiciona um chamado na fila com delay de 3 minutos
    public void enfileirar(ChamadoRequest request) {
        fila.offer(new ChamadoEmEspera(request));
    }

    // Tenta pegar o próximo chamado PRONTO (delay vencido).
    // Se nenhum estiver pronto, retorna null imediatamente — não bloqueia.
    // poll() vs take(): poll() não bloqueia (retorna null se vazio ou não pronto).
    //                   take() bloqueia a thread até haver algo disponível.
    //                   Usamos poll() para o scheduler não travar.
    public ChamadoEmEspera poll() {
        return fila.poll();
    }

    public int tamanho() {
        return fila.size();
    }
}
