package com.example.Projeto.pratico.enums;

import java.util.EnumSet;
import java.util.Set;

public enum StatusChamado {
 
    ABERTO {
        @Override
        public Set<StatusChamado> transicoesPermitidas() {
            return EnumSet.of(EM_ESPERA, EM_ATENDIMENTO);
        }
    },
 
    EM_ESPERA {
        @Override
        public Set<StatusChamado> transicoesPermitidas() {
            // Só avança quando o scheduler encontrar espaço no balcão
            return EnumSet.of(EM_ATENDIMENTO);
        }
    },
 
    EM_ATENDIMENTO {
        @Override
        public Set<StatusChamado> transicoesPermitidas() {
            // Só conclui — não volta para estados anteriores
            return EnumSet.of(CONCLUIDO);
        }
    },
 
    CONCLUIDO {
        @Override
        public Set<StatusChamado> transicoesPermitidas() {
            // Estado terminal: nenhuma saída possível
            return EnumSet.noneOf(StatusChamado.class);
        }
    };
 
    public abstract Set<StatusChamado> transicoesPermitidas();
 
    /**
     * Verifica se é possível transicionar deste estado para o destino.
     */
    public boolean podeTransicionarPara(StatusChamado destino) {
        return transicoesPermitidas().contains(destino);
    }
}