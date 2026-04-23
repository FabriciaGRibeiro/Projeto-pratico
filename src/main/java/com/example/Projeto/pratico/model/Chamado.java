package com.example.Projeto.pratico.model;

import com.example.Projeto.pratico.enums.StatusChamado;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade que representa a tabela tb_chamado no banco de dados.
 * Um chamado é aberto por um cliente (customer_id) para um dispositivo
 * específico e percorre um ciclo de vida definido pelo StatusChamado.
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

    // ID do cliente que abriu o chamado (referência ao usuário/customer)
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    // ID do dispositivo com problema
    @Column(name = "device_id")
    private String deviceId;

    // Número de série do dispositivo
    @Column(name = "serial_number")
    private String serialNumber;

    // Descrição do problema relatado pelo cliente
    @Column(nullable = false, length = 500)
    private String motivo;

    // Nome do produto (ex: "Maquininha Point Pro")
    @Column(nullable = false)
    private String produto;

    // -------------------------------------------------------------------------
    // @Enumerated(EnumType.STRING)
    // → Salva o NOME do enum no banco (ex: "ABERTO"), não o número (0, 1, 2...)
    //   Isso é importante! Se você usar ORDINAL (número) e adicionar um novo
    //   valor no meio do enum, todos os dados ficam errados. STRING é sempre
    //   mais seguro e legível no banco.
    // -------------------------------------------------------------------------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusChamado status = StatusChamado.ABERTO;

    // -------------------------------------------------------------------------
    // Relacionamento Many-to-One com Balcao
    //
    // @ManyToOne  → muitos chamados podem estar em um mesmo balcão
    // @JoinColumn → define qual coluna é a chave estrangeira (FK) nesta tabela
    // LAZY        → só carrega os dados do balcão quando necessário
    //
    // Diferente de guardar só o balcaoId (Long), aqui temos o objeto completo.
    // Isso permite acessar chamado.getBalcao().getNomeAtendente() diretamente.
    // -------------------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "balcao_id")
    private Balcao balcao;

    // Data em que o chamado foi criado pelo sistema
    @Column(name = "data_criacao", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dataCriacao;

    // Data em que o chamado foi resolvido — NULL enquanto não concluído
    @Column(name = "data_resolucao")
    private LocalDateTime dataResolucao;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
