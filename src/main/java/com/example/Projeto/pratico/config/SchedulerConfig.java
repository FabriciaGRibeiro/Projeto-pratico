package com.example.Projeto.pratico.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling ativa o suporte a tarefas agendadas no Spring.
// Sem isso, o @Scheduled no ProcessadorDeFilaScheduler é ignorado —
// o método existe, mas nunca é chamado automaticamente.
//
// Separamos em uma classe de config para não poluir a classe principal (HelpDesk.java).
@Configuration
@EnableScheduling
public class SchedulerConfig {
}
