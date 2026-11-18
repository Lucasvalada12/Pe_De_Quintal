package br.edu.aula.pedequintal.config;

import br.edu.aula.pedequintal.model.Usuario;
import br.edu.aula.pedequintal.repository.UsuarioRepository;
import br.edu.aula.pedequintal.service.PropostaService;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Vale para TODOS os controllers:
 * - coloca no Model o usuário logado e o nº de propostas pendentes (usados no header);
 * - trata dois erros "globais" com uma mensagem amigável.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    private final UsuarioRepository usuarios;
    private final PropostaService propostaService;

    public GlobalControllerAdvice(UsuarioRepository usuarios, PropostaService propostaService) {
        this.usuarios = usuarios;
        this.propostaService = propostaService;
    }

    @ModelAttribute("usuarioLogado")
    public Usuario usuarioLogado(Principal principal) {
        if (principal == null) return null;          // visitante (telas de login/cadastro)
        return usuarios.findByEmailIgnoreCase(principal.getName()).orElse(null);
    }

    @ModelAttribute("pendentesRecebidas")
    public long pendentesRecebidas(Principal principal) {
        if (principal == null) return 0;
        return usuarios.findByEmailIgnoreCase(principal.getName())
                .map(propostaService::contarRecebidasPendentes)
                .orElse(0L);
    }

    /** Foto maior que o limite de spring.servlet.multipart.max-file-size. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String fotoGrandeDemais(RedirectAttributes flash) {
        flash.addFlashAttribute("erro", "A foto é grande demais. Envie uma imagem de até 5 MB.");
        return "redirect:/itens/novo";
    }

    /** Dois usuários alteraram o mesmo anúncio ao mesmo tempo (@Version no Anuncio). */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public String conflitoDeConcorrencia(RedirectAttributes flash) {
        flash.addFlashAttribute("erro",
                "Este anúncio foi alterado por outra ação ao mesmo tempo. Confira a situação atual e tente de novo.");
        return "redirect:/propostas";
    }
}
