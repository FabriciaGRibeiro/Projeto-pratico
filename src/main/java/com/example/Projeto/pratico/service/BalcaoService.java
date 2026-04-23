package com.example.Projeto.pratico.service;

import com.example.Projeto.pratico.dto.BalcaoRequest;
import com.example.Projeto.pratico.dto.BalcaoResponse;
import com.example.Projeto.pratico.exception.RecursoNaoEncontradoException;
import com.example.Projeto.pratico.model.Balcao;
import com.example.Projeto.pratico.repository.BalcaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// @Service = marca essa classe como um componente de lógica de negócio.
// O Spring gerencia o ciclo de vida dessa classe (cria uma instância e
// injeta onde for necessário).
//
// A camada de Service existe para separar responsabilidades:
//   Controller → recebe e responde requisições HTTP
//   Service    → executa a lógica de negócio
//   Repository → fala com o banco de dados
//
// @RequiredArgsConstructor (Lombok) = gera um construtor com todos os campos
// "final". É a forma recomendada de fazer injeção de dependência em Spring.
@Service
@RequiredArgsConstructor
public class BalcaoService {

    private final BalcaoRepository balcaoRepository;

    public BalcaoResponse criar(BalcaoRequest request) {
        Balcao balcao = Balcao.builder()
                .nomeAtendente(request.nomeAtendente())
                .build();

        Balcao salvo = balcaoRepository.save(balcao);
        return BalcaoResponse.from(salvo);
    }

    public List<BalcaoResponse> listarTodos() {
        return balcaoRepository.findAll()
                .stream()
                .map(BalcaoResponse::from)
                .toList();
    }

    public BalcaoResponse buscarPorId(Long id) {
        Balcao balcao = balcaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Balcão não encontrado com id: " + id
                ));
        return BalcaoResponse.from(balcao);
    }

    public BalcaoResponse atualizar(Long id, BalcaoRequest request) {
        // Verificamos se existe antes de tentar atualizar.
        // Sem isso, o save() criaria um novo registro em vez de atualizar.
        Balcao balcao = balcaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Balcão não encontrado com id: " + id
                ));

        balcao.setNomeAtendente(request.nomeAtendente());
        Balcao atualizado = balcaoRepository.save(balcao);
        return BalcaoResponse.from(atualizado);
    }

    public void deletar(Long id) {
        if (!balcaoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Balcão não encontrado com id: " + id);
        }
        balcaoRepository.deleteById(id);
    }
}
