package com.example.Projeto.pratico.repository;

import com.example.Projeto.pratico.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Spring Data gera o SQL automaticamente a partir do nome do método:
    // SELECT * FROM tb_usuario WHERE username = ?
    Optional<Usuario> findByUsername(String username);
}
