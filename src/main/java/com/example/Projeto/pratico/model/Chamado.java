package com.example.Projeto.pratico.model;

import com.example.Projeto.pratico.enums.StatusChamado;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade que representa a tabela tb_chamado no banco de dados.
 *
 * Timestamps do ciclo de vida:
 *   dataCriacao          → quando o chamado foi criado (imutável)
 *   dataInicioAtendimento → quando o job moveu para EM_ATENDIMENTO (base do timer de 2 min)
 *   dataResolucao        → quando o chamado foi concluído
 */
@Entity
@Table(name = "tb_chamado")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(nullable = false)
    private String produto;

    // @Enumerated(EnumType.STRING) → salva o nome do enum ("ABERTO"), não o índice (0).
    // Usar índice é perigoso: adicionar um valor no meio do enum corromperia todos os dados.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusChamado status = StatusChamado.ABERTO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "balcao_id")
    private Balcao balcao;

    // Preenchido automaticamente na criação, nunca alterado
    @Column(name = "data_criacao", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dataCriacao;

    // Preenchido pela MaquinaDeEstadosChamado ao transicionar para EM_ATENDIMENTO.
    // Usado pelo ProcessamentoChamadoScheduler para saber quando os 2 minutos expiraram.
    @Column(name = "data_inicio_atendimento")
    private LocalDateTime dataInicioAtendimento;

    // Preenchido pela MaquinaDeEstadosChamado ao transicionar para CONCLUIDO.
    // NULL enquanto o chamado não estiver concluído.
    @Column(name = "data_resolucao")
    private LocalDateTime dataResolucao;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}