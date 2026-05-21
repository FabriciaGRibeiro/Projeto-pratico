package com.example.Projeto.pratico.config;

import com.example.Projeto.pratico.model.Usuario;
import com.example.Projeto.pratico.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// -------------------------------------------------------------------------
// Roda uma única vez após a aplicação subir.
// Garante que sempre existam usuários iniciais para testar a autenticação.
//
// Por que não usar SQL na migration?
// Porque a senha precisa ser hasheada com BCrypt. O hash não pode ser
// calculado manualmente de forma confiável em SQL. Aqui usamos o
// PasswordEncoder bean que o Spring já configura corretamente.
// -------------------------------------------------------------------------
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() == 0) {
            usuarioRepository.save(Usuario.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .build());

            usuarioRepository.save(Usuario.builder()
                    .username("atendente")
                    .password(passwordEncoder.encode("atendente123"))
                    .role("ATENDENTE")
                    .build());
        }
    }
}
