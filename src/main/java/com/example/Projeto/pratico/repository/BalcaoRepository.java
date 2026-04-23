package com.example.Projeto.pratico.repository;

import com.example.Projeto.pratico.model.Balcao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BalcaoRepository extends JpaRepository<Balcao, Long> {

    // Encontra o balcão com MENOS chamados ativos E que ainda tem capacidade disponível.
    //
    // HAVING COUNT(c.id) < 5
    //   → Filtra apenas balcões que têm MENOS de 5 chamados ativos.
    //     Se um balcão já tem 5, ele é excluído do resultado.
    //     Se todos os balcões estão com 5, o Optional retorna vazio.
    //
    // O resto da lógica é a mesma de antes:
    //   LEFT JOIN → balcões sem chamados aparecem com COUNT = 0
    //   ORDER BY COUNT ASC → o menos ocupado vem primeiro
    //   LIMIT 1 → pega só o primeiro
    @Query(value = """
            SELECT b.* FROM tb_balcao b
            LEFT JOIN tb_chamado c
                ON c.balcao_id = b.id
                AND c.status IN ('ABERTO', 'EM_ESPERA', 'EM_ATENDIMENTO')
            GROUP BY b.id
            HAVING COUNT(c.id) < 5
            ORDER BY COUNT(c.id) ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Balcao> findBalcaoComMenosChamadosAtivos();
}
