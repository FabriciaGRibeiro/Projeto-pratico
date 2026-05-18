package com.example.Projeto.pratico.client;

import com.example.Projeto.pratico.dto.ClienteInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Slf4j
@Component
public class DataVaultClient {

    private final RestClient restClient;

    public DataVaultClient(@Value("${datavault.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Busca os dados de um cliente no Data Vault.
     * Retorna Optional.empty() se o serviço estiver indisponível ou o cliente não for encontrado.
     */
    public Optional<ClienteInfo> buscarCliente(Long customerId) {
        try {
            ClienteInfo cliente = restClient.get()
                    .uri("/customers/{id}", customerId)
                    .retrieve()
                    .body(ClienteInfo.class);
            return Optional.ofNullable(cliente);
        } catch (RestClientException e) {
            log.warn("[DataVault] Falha ao buscar cliente id={}: {}", customerId, e.getMessage());
            return Optional.empty();
        }
    }
}
