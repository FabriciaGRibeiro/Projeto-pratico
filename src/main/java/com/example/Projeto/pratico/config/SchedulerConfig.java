package com.example.Projeto.pratico.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// habilita o agendamento de tarefas. Sem isso, o método anotado com @Scheduled não será executado.

@Configuration
@EnableScheduling
public class SchedulerConfig {
}
