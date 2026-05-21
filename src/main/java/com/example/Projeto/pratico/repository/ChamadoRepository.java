package com.example.Projeto.pratico.repository;

import com.example.Projeto.pratico.enums.StatusChamado;
import com.example.Projeto.pratico.model.Chamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    // -------------------------------------------------------------------------
    // Paginação — usadas pelo ChamadoService para listagens
    // -------------------------------------------------------------------------
    Page<Chamado> findAll(Pageable pageable);

    Page<Chamado> findByCustomerId(Long customerId, Pageable pageable);

    // -------------------------------------------------------------------------
    // Usada pelo ValidadorChamado para checar capacidade do balcão
    // -------------------------------------------------------------------------
    long countByBalcaoIdAndStatusIn(Long balcaoId, List<StatusChamado> statuses);

    // -------------------------------------------------------------------------
    // Regras de conflito (ChamadoService.criar)
    //
    // Busca o chamado mais recente de um serial que ainda não foi concluído.
    // Retorna Optional.empty() se só existirem chamados CONCLUIDOS para esse serial,
    // o que significa que um novo chamado pode ser criado.
    // -------------------------------------------------------------------------
    @Query("""
            SELECT c FROM Chamado c
            WHERE c.serialNumber = :serialNumber
              AND c.status <> 'CONCLUIDO'
            ORDER BY c.dataCriacao DESC
            LIMIT 1
            """)
    Optional<Chamado> findChamadoAbertoBySerialNumber(@Param("serialNumber") String serialNumber);

    // -------------------------------------------------------------------------
    // Usada pelo ProcessamentoChamadoScheduler - Step 1
    // Busca todos os chamados ABERTO para mover para EM_ATENDIMENTO
    // -------------------------------------------------------------------------
    List<Chamado> findByStatus(StatusChamado status);

    // -------------------------------------------------------------------------
    // Usada pelo ProcessamentoChamadoScheduler - Step 2
    // Busca chamados EM_ATENDIMENTO cujo timer de 2 minutos já expirou
    //
    // dataInicioAtendimento <= limite significa: iniciou há mais de 2 minutos atrás
    // O índice criado na V3 garante performance nessa query
    // -------------------------------------------------------------------------
    List<Chamado> findByStatusAndDataInicioAtendimentoBefore(
            StatusChamado status,
            LocalDateTime limite
    );
}