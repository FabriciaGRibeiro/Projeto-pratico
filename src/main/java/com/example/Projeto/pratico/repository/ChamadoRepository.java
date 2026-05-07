package com.example.Projeto.pratico.repository;

import com.example.Projeto.pratico.enums.StatusChamado;
import com.example.Projeto.pratico.model.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    long countByBalcaoIdAndStatusIn(Long balcaoId, List<StatusChamado> statuses);
}
