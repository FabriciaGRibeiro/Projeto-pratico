package com.example.Projeto.pratico.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// -------------------------------------------------------------------------
// Esta classe configura o Servidor de Recursos (Resource Server).
//
// O que é um Resource Server?
// É a parte da aplicação que PROTEGE os endpoints. Quando uma requisição
// chega com um token JWT no header "Authorization: Bearer <token>", o
// Resource Server valida esse token e decide se a requisição pode continuar.
//
// Fluxo completo:
//   1. Cliente pede token → Authorization Server (/oauth2/token)
//   2. AS autentica o usuário e emite um JWT assinado
//   3. Cliente usa o JWT para acessar /balcoes, /chamados, etc.
//   4. Resource Server (aqui) valida a assinatura e as permissões do JWT
//
// @Order(2) → esta filter chain é avaliada DEPOIS do AuthorizationServerConfig
// (que tem @Order(1)). Trata todos os endpoints da aplicação.
// -------------------------------------------------------------------------
@Configuration
public class SecurityConfig {

    @Bean
    @Order(2)
    public SecurityFilterChain appSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF desabilitado para APIs REST (não usam sessão/cookies de formulário)
            .csrf(AbstractHttpConfigurer::disable)

            .authorizeHttpRequests(auth -> auth
                // Libera a página de login e os endpoints do protocolo OAuth2
                .requestMatchers("/login", "/error").permitAll()
                // Todos os endpoints da API exigem autenticação via JWT
                .requestMatchers("/balcoes/**", "/chamados/**").authenticated()
                .anyRequest().authenticated()
            )
            // Habilita formulário de login padrão do Spring.
            // Necessário para o fluxo Authorization Code: o AS redireciona
            // o usuário para /login antes de emitir o token.
            .formLogin(Customizer.withDefaults())
            // Configura esta aplicação como Resource Server: valida o JWT
            // presente no header "Authorization: Bearer <token>" de cada requisição.
            .oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()));

        return http.build();
    }

    // BCryptPasswordEncoder é o algoritmo de hash recomendado para senhas.
    // Ele adiciona um "salt" aleatório em cada hash, tornando ataques de
    // dicionário ineficazes. NUNCA armazene senhas em texto puro.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
