package com.example.Projeto.pratico.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// -------------------------------------------------------------------------
// Implementar UserDetails diretamente na entidade é um padrão comum em
// projetos Spring Boot. Isso evita criar um objeto intermediário (wrapper)
// só para satisfazer o contrato do Spring Security.
//
// O Spring Security, quando autentica um usuário, espera um objeto UserDetails.
// Ao implementar a interface aqui, a entidade JPA já serve como esse objeto.
// -------------------------------------------------------------------------
@Entity
@Table(name = "tb_usuario")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // Role define o nível de acesso: USER, ADMIN, ATENDENTE, etc.
    @Column(nullable = false)
    @Builder.Default
    private String role = "USER";

    // Retorna as permissões do usuário. O Spring Security usa "ROLE_" como prefixo padrão.
    // Ex: role = "ADMIN" → authority = "ROLE_ADMIN"
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    // Os métodos abaixo fazem parte do contrato UserDetails.
    // Retornam true para manter as contas sempre ativas neste projeto.
    // Em produção, você pode adicionar campos no banco para controlar cada um.
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
