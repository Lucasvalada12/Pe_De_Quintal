package br.edu.aula.pedequintal.service;

import br.edu.aula.pedequintal.dto.CadastroForm;
import br.edu.aula.pedequintal.model.Usuario;
import br.edu.aula.pedequintal.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrar(CadastroForm form) {
        String email = form.getEmail().trim().toLowerCase();

        // Evita a "pegadinha" do save(): conferimos antes se o e-mail já existe
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Já existe uma conta com este e-mail.");
        }

        Usuario usuario = new Usuario(
                form.getNome().trim(),
                email,
                passwordEncoder.encode(form.getSenha()),   // BCrypt
                form.getTelefone().trim(),
                form.getCidade().trim(),
                form.getUf().trim().toUpperCase());

        return usuarios.save(usuario);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarios.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
    }
}
