package br.edu.aula.pedequintal.service;

import br.edu.aula.pedequintal.model.Usuario;
import br.edu.aula.pedequintal.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Ponte entre o Spring Security e a nossa tabela "usuario".
 * No login, o Spring chama loadUserByUsername(email) e compara a senha digitada
 * com o hash BCrypt guardado no banco.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public UsuarioDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarios.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("E-mail não cadastrado"));

        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .roles("USUARIO")
                .build();
    }
}
