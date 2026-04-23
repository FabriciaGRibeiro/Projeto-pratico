package com.example.Projeto.pratico.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade que representa a tabela tb_balcao no banco de dados.
 *
 * @Entity  → diz ao Spring/Hibernate que essa classe é uma tabela no banco
 * @Table   → define o nome real da tabela
 * @Data    → Lombok gera getters, setters, equals, hashCode e toString
 * @Builder → permite construir objetos com sintaxe fluente: Balcao.builder().nome("A").build()
 */
@Entity
@Table(name = "tb_balcao")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Balcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(name = "...") → mapeia o campo Java para a coluna exata no banco
    @Column(name = "nome_atendente", nullable = false)
    private String nomeAtendente;

    @CreationTimestamp
    // updatable = false → uma vez gravada, a data de criação nunca é alterada
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    // -------------------------------------------------------------------------
    // Relacionamentos (não geram colunas em tb_balcao — a FK fica nas outras tabelas)
    //
    // @OneToMany  → um balcão tem muitos atendentes
    // mappedBy    → diz que o "dono" do relacionamento é o campo "balcao" em Atendente
    // LAZY        → só carrega os atendentes do banco quando alguém acessar a lista
    //               (evita carregar dados desnecessários a cada consulta)
    // -------------------------------------------------------------------------
    @OneToMany(mappedBy = "balcao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Atendente> atendentes;

    @OneToMany(mappedBy = "balcao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Fila> filas;
}
