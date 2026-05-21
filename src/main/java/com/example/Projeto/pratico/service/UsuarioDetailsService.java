package com.example.Projeto.pratico.service;

import com.example.Projeto.pratico.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// -------------------------------------------------------------------------
// UserDetailsService é a "ponte" entre o Spring Security e o nosso banco.
//
// Quando o usuário tenta fazer login, o Spring Security chama
// loadUserByUsername(username). Nós buscamos no banco e retornamos o
// objeto Usuario (que implementa UserDetails). O Spring faz o resto:
// compara a senha, verifica permissões, etc.
// -------------------------------------------------------------------------
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }
}
