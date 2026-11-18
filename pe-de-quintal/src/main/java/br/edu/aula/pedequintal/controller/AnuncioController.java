package br.edu.aula.pedequintal.controller;

import br.edu.aula.pedequintal.dto.AnuncioForm;
import br.edu.aula.pedequintal.dto.PropostaForm;
import br.edu.aula.pedequintal.model.Anuncio;
import br.edu.aula.pedequintal.model.Modalidade;
import br.edu.aula.pedequintal.model.Usuario;
import br.edu.aula.pedequintal.service.AnuncioService;
import br.edu.aula.pedequintal.service.PropostaService;
import br.edu.aula.pedequintal.service.RegraNegocioException;
import br.edu.aula.pedequintal.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Objects;

/** W2 (lista), detalhe + enviar proposta, W3 (publicar) e exclusão do próprio anúncio. */
@Controller
public class AnuncioController {

    private final AnuncioService anuncioService;
    private final PropostaService propostaService;
    private final UsuarioService usuarioService;

    public AnuncioController(AnuncioService anuncioService, PropostaService propostaService,
                             UsuarioService usuarioService) {
        this.anuncioService = anuncioService;
        this.propostaService = propostaService;
        this.usuarioService = usuarioService;
    }

    // ---------------------------------------------------------- W2 — lista

    @GetMapping("/itens")
    public String listar(@RequestParam(required = false) String busca,
                         @RequestParam(required = false) Integer categoria,
                         @RequestParam(required = false) String cidade,
                         Model model) {
        model.addAttribute("anuncios", anuncioService.listarDisponiveis(busca, categoria, cidade));
        model.addAttribute("categorias", anuncioService.listarCategorias());
        model.addAttribute("cidades", anuncioService.listarCidades());
        // devolve os filtros para a página manter o que o usuário escolheu
        model.addAttribute("busca", busca);
        model.addAttribute("categoriaSelecionada", categoria);
        model.addAttribute("cidadeSelecionada", cidade);
        return "itens/lista";
    }

    // ------------------------------------------------------- W3 — publicar

    @GetMapping("/itens/novo")
    public String telaPublicar(Model model) {
        model.addAttribute("anuncioForm", new AnuncioForm());
        prepararFormulario(model);
        return "itens/novo";
    }

    @PostMapping("/itens")
    public String publicar(@Valid @ModelAttribute("anuncioForm") AnuncioForm form,
                           BindingResult erros,
                           @RequestParam(value = "foto", required = false) MultipartFile foto,
                           Principal principal,
                           Model model,
                           RedirectAttributes flash) {
        if (erros.hasErrors()) {
            prepararFormulario(model);
            return "itens/novo";
        }
        try {
            Anuncio anuncio = anuncioService.publicar(form, foto, usuarioAtual(principal));
            flash.addFlashAttribute("sucesso", "Anúncio publicado! Agora é só aguardar as propostas.");
            return "redirect:/itens/" + anuncio.getId();
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            prepararFormulario(model);
            return "itens/novo";
        }
    }

    // ------------------------------------------------------------- detalhe

    @GetMapping("/itens/{id}")
    public String detalhe(@PathVariable Integer id, Principal principal, Model model, RedirectAttributes flash) {
        Anuncio anuncio;
        try {
            anuncio = anuncioService.buscarPorId(id);
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
            return "redirect:/itens";
        }
        Usuario usuario = usuarioAtual(principal);
        boolean ehDono = Objects.equals(anuncio.getUsuario().getId(), usuario.getId());
        boolean jaPropos = !ehDono && propostaService.temPropostaPendente(id, usuario);

        model.addAttribute("anuncio", anuncio);
        model.addAttribute("ehDono", ehDono);
        model.addAttribute("jaPropos", jaPropos);
        model.addAttribute("podePropor", !ehDono && !jaPropos && anuncio.isAtivo());
        model.addAttribute("contatoLiberado", !ehDono && propostaService.temPropostaAceita(id, usuario));
        if (!model.containsAttribute("propostaForm")) {
            model.addAttribute("propostaForm", new PropostaForm());
        }
        return "itens/detalhe";
    }

    // ----------------------------------------------------- enviar proposta

    @PostMapping("/itens/{id}/propostas")
    public String enviarProposta(@PathVariable Integer id,
                                 @Valid @ModelAttribute("propostaForm") PropostaForm form,
                                 BindingResult erros,
                                 Principal principal,
                                 RedirectAttributes flash) {
        if (erros.hasErrors()) {
            flash.addFlashAttribute("erro", erros.getAllErrors().get(0).getDefaultMessage());
            flash.addFlashAttribute("propostaForm", form);
            return "redirect:/itens/" + id;
        }
        try {
            propostaService.enviar(id, form, usuarioAtual(principal));
            flash.addFlashAttribute("sucesso", "Proposta enviada! Acompanhe em \"Minhas propostas\".");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
            flash.addFlashAttribute("propostaForm", form);
        }
        return "redirect:/itens/" + id;
    }

    // -------------------------------------------------------------- excluir

    @PostMapping("/itens/{id}/excluir")
    public String excluir(@PathVariable Integer id, Principal principal, RedirectAttributes flash) {
        try {
            anuncioService.excluir(id, usuarioAtual(principal));
            flash.addFlashAttribute("sucesso", "Anúncio excluído.");
        } catch (RegraNegocioException e) {
            flash.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/propostas";
    }

    // ---------------------------------------------------------------- apoio

    private void prepararFormulario(Model model) {
        model.addAttribute("categorias", anuncioService.listarCategorias());
        model.addAttribute("unidades", AnuncioService.UNIDADES);
        model.addAttribute("modalidades", Modalidade.values());
    }

    private Usuario usuarioAtual(Principal principal) {
        return usuarioService.buscarPorEmail(principal.getName());
    }
}
