package com.example.Projeto.pratico.service;

import com.example.Projeto.pratico.dto.ChamadoRequest;
import com.example.Projeto.pratico.queue.ChamadoEmEspera;
import org.springframework.stereotype.Component;

import java.util.concurrent.DelayQueue;

@Component
public class FilaDeEspera {

    private final DelayQueue<ChamadoEmEspera> fila = new DelayQueue<>();

    // Adiciona um chamado na fila com delay de 3 minutos
    public void enfileirar(ChamadoRequest request) {
        fila.offer(new ChamadoEmEspera(request));
    }

    public ChamadoEmEspera poll() {
        return fila.poll();
    }

    public int tamanho() {
        return fila.size();
    }
}
