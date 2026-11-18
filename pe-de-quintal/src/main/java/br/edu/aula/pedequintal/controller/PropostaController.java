package br.edu.aula.pedequintal.controller;

import br.edu.aula.pedequintal.model.Usuario;
import br.edu.aula.pedequintal.service.AnuncioService;
import br.edu.aula.pedequintal.service.PropostaService;
import br.edu.aula.pedequintal.service.RegraNegocioException;
import br.edu.aula.pedequintal.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** W4 — Minhas propostas (recebidas/enviadas) + meus anúncios. */
@Controller
public class PropostaController {

    private final PropostaService propostaService;
    private final AnuncioService anuncioService;
    private final UsuarioService usuarioService;

    public PropostaController(PropostaService propostaService, AnuncioService anuncioService,
                              UsuarioService usuarioService) {
        this.propostaService = propostaService;
        this.anuncioService = anuncioService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/propostas")
    public String listar(@RequestParam(defaultValue = "recebidas") String aba, Principal principal, Model model) {
        Usuario usuario = usuarioAtual(principal);
        model.addAttribute("aba", "enviadas".equals(aba) ? "enviadas" : "recebidas");
        model.addAttribute("recebidas", propostaService.listarRecebidas(usuario));
        model.addAttribute("enviadas", propostaService.listarEnviadas(usuario));
        model.addAttribute("meusAnuncios", anuncioService.listarDoUsuario(usuario));
        return "propostas";
    }

    @PostMapping("/propostas/{id}/aceitar")
    public String aceitar(@PathVariable Integer id, Principal principal, RedirectAttributes flash) {
        try {
            propostaService.aceitar(id, usuarioAtual(principal));
            flash.addFlashAttribute("sucesso",
                    "Proposta aceita! O contato foi liberado — combine a retirada e depois confirme a entrega.");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/propostas";
    }

    @PostMapping("/propostas/{id}/rejeitar")
    public String rejeitar(@PathVariable Integer id, Principal principal, RedirectAttributes flash) {
        try {
            propostaService.rejeitar(id, usuarioAtual(principal));
            flash.addFlashAttribute("sucesso", "Proposta rejeitada.");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/propostas";
    }

    @PostMapping("/propostas/{id}/concluir")
    public String concluir(@PathVariable Integer id,
                           @RequestParam(defaultValue = "recebidas") String aba,
                           Principal principal, RedirectAttributes flash) {
        try {
            propostaService.concluir(id, usuarioAtual(principal));
            flash.addFlashAttribute("sucesso", "Entrega confirmada. Obrigado por evitar o desperdício! 🌱");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "enviadas".equals(aba) ? "redirect:/propostas?aba=enviadas" : "redirect:/propostas";
    }

    private Usuario usuarioAtual(Principal principal) {
        return usuarioService.buscarPorEmail(principal.getName());
    }
}
