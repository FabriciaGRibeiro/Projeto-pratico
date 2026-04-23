package com.example.Projeto.pratico.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

// O projeto usa Spring Security, que por padrão bloqueia TODAS as requisições
// exigindo autenticação. Sem essa config, qualquer chamada para /balcoes
// retornaria 401 (Unauthorized) antes de chegar no controller.
//
// Aqui configuramos as regras de acesso. Por enquanto, liberamos /balcoes/**
// para facilitar o desenvolvimento. A segurança completa (OAuth2, tokens) será
// configurada em uma próxima etapa.
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita CSRF para APIs REST.
            // CSRF é uma proteção para aplicações que usam sessão + cookies (ex: formulários HTML).
            // APIs REST com JSON não usam sessão, então essa proteção não se aplica.
            .csrf(AbstractHttpConfigurer::disable)

            .authorizeHttpRequests(auth -> auth
                // Libera os endpoints de balcões e chamados sem autenticação
                .requestMatchers("/balcoes/**", "/chamados/**").permitAll()
                // Todo o resto exige autenticação
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
