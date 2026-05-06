package com.example.Projeto.pratico.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

// -------------------------------------------------------------------------
// Esta classe configura o Servidor de Autorização OAuth2.
//
// O que é um Servidor de Autorização?
// É o componente responsável por EMITIR tokens JWT após autenticar o usuário.
// Pensa nele como o "balcão de emissão de senhas temporárias":
//   1. O cliente (frontend, Postman, app) pede um token
//   2. O usuário faz login
//   3. O servidor emite um JWT assinado
//   4. O cliente usa esse JWT para acessar os endpoints protegidos
//
// @Order(1) → esta filter chain tem prioridade máxima e trata os
// endpoints do protocolo OAuth2: /oauth2/token, /oauth2/authorize, etc.
// -------------------------------------------------------------------------
@Configuration
public class AuthorizationServerConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain authServerFilterChain(HttpSecurity http) throws Exception {
        // Nova API do Spring Authorization Server 1.4+ (substitui applyDefaultSecurity)
        OAuth2AuthorizationServerConfigurer authServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        http
            // Aplica esta filter chain SOMENTE nos endpoints OAuth2 (/oauth2/token, /oauth2/authorize, etc.)
            .securityMatcher(authServerConfigurer.getEndpointsMatcher())
            .with(authServerConfigurer, as ->
                as.oidc(Customizer.withDefaults())) // Habilita OpenID Connect (retorna info do usuário)
            .exceptionHandling(ex -> ex
                // Quando um browser tenta acessar sem token, redireciona para login
                // em vez de retornar 401 (comportamento amigável para navegadores)
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
            )
            // O próprio Authorization Server valida tokens JWT para seu endpoint /userinfo
            .oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()));

        return http.build();
    }

    // -------------------------------------------------------------------------
    // RegisteredClient = aplicação que tem permissão de pedir tokens.
    //
    // Pensa assim: assim como um usuário precisa de login/senha para entrar,
    // uma aplicação precisa de clientId/clientSecret para pedir tokens.
    //
    // Aqui usamos InMemory (em memória) para simplificar.
    // Em produção, isso viria do banco de dados (JdbcRegisteredClientRepository).
    // -------------------------------------------------------------------------
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("helpdesk-client")
                .clientSecret("{noop}helpdesk-secret") // {noop} = sem hash, só para dev
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // Fluxo padrão OAuth2
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:8080/authorized") // Onde o token é entregue após login
                .scope(OidcScopes.OPENID) // Permite retornar dados do usuário logado
                .scope("read")
                .scope("write")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))    // Token expira em 1h
                        .refreshTokenTimeToLive(Duration.ofDays(7))    // Refresh token dura 7 dias
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(client);
    }

    // -------------------------------------------------------------------------
    // JWKSource = fonte das chaves criptográficas usadas para ASSINAR os tokens.
    //
    // Usamos RSA 2048 bits: uma chave privada para assinar, uma pública para
    // verificar. Qualquer serviço com a chave pública pode verificar se o token
    // é legítimo, sem precisar da chave privada.
    //
    // ATENÇÃO: generateRsaKey() gera um par novo a cada restart.
    // Em produção, a chave deve ser persistida (keystore, AWS KMS, etc.)
    // para que tokens emitidos antes do restart continuem válidos.
    // -------------------------------------------------------------------------
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsaKey();
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    private RSAKey generateRsaKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar par de chaves RSA", e);
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    // Define a URL base do servidor. Aparece dentro do próprio token JWT
    // no campo "iss" (issuer), para que quem receba o token saiba de onde veio.
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:8080")
                .build();
    }
}
