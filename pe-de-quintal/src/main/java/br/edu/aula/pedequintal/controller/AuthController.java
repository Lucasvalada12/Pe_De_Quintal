package br.edu.aula.pedequintal.controller;

import br.edu.aula.pedequintal.dto.CadastroForm;
import br.edu.aula.pedequintal.service.RegraNegocioException;
import br.edu.aula.pedequintal.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** W1 — Entrar / criar conta. O POST /login é tratado pelo próprio Spring Security. */
@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login(Principal principal) {
        if (principal != null) {
            return "redirect:/itens";   // já está logado
        }
        return "login";
    }

    @GetMapping("/cadastro")
    public String telaCadastro(Model model) {
        model.addAttribute("cadastroForm", new CadastroForm());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("cadastroForm") CadastroForm form,
                            BindingResult erros,
                            RedirectAttributes flash) {
        if (erros.hasErrors()) {
            return "cadastro";   // volta para o formulário mostrando os erros de cada campo
        }
        try {
            usuarioService.cadastrar(form);
        } catch (RegraNegocioException e) {
            erros.rejectValue("email", "duplicado", e.getMessage());
            return "cadastro";
        }
        // Post/Redirect/Get: F5 na próxima página não reenvia o cadastro
        flash.addFlashAttribute("sucesso", "Conta criada! Entre com seu e-mail e senha.");
        return "redirect:/login";
    }
}
