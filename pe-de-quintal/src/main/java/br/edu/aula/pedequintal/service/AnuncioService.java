package br.edu.aula.pedequintal.service;

import br.edu.aula.pedequintal.dto.AnuncioForm;
import br.edu.aula.pedequintal.model.Anuncio;
import br.edu.aula.pedequintal.model.Categoria;
import br.edu.aula.pedequintal.model.Modalidade;
import br.edu.aula.pedequintal.model.StatusAnuncio;
import br.edu.aula.pedequintal.model.Usuario;
import br.edu.aula.pedequintal.repository.AnuncioRepository;
import br.edu.aula.pedequintal.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AnuncioService {

    /** Unidades aceitas no formulário (o <select> da W3 usa esta mesma lista). */
    public static final List<String> UNIDADES = List.of("kg", "g", "unidade", "maço", "pé", "dúzia", "muda");

    private final AnuncioRepository anuncios;
    private final CategoriaRepository categorias;
    private final FotoService fotoService;

    public AnuncioService(AnuncioRepository anuncios, CategoriaRepository categorias, FotoService fotoService) {
        this.anuncios = anuncios;
        this.categorias = categorias;
        this.fotoService = fotoService;
    }

    /** W2: apenas ATIVOS e dentro do prazo "disponível até" (sem job agendado). */
    public List<Anuncio> listarDisponiveis(String busca, Integer categoriaId, String cidade) {
        return anuncios.buscarDisponiveis(
                StatusAnuncio.ATIVO,
                LocalDate.now(),
                busca == null ? "" : busca.trim(),
                categoriaId == null ? 0 : categoriaId,
                cidade == null ? "" : cidade.trim());
    }

    public List<String> listarCidades() {
        return anuncios.listarCidadesComAnuncios(StatusAnuncio.ATIVO, LocalDate.now());
    }

    public List<Categoria> listarCategorias() {
        return categorias.findAllByOrderByNome();
    }

    public Anuncio buscarPorId(Integer id) {
        return anuncios.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Anúncio não encontrado."));
    }

    public List<Anuncio> listarDoUsuario(Usuario usuario) {
        return anuncios.findByUsuarioIdOrderByDataCriacaoDesc(usuario.getId());
    }

    @Transactional
    public Anuncio publicar(AnuncioForm form, MultipartFile foto, Usuario anunciante) {
        Categoria categoria = categorias.findById(form.getCategoriaId())
                .orElseThrow(() -> new RegraNegocioException("Categoria inválida."));

        if (!UNIDADES.contains(form.getUnidade())) {
            throw new RegraNegocioException("Unidade inválida.");
        }

        Anuncio anuncio = new Anuncio();
        anuncio.setTitulo(form.getTitulo().trim());
        anuncio.setDescricao(textoOuNulo(form.getDescricao()));
        anuncio.setCategoria(categoria);
        anuncio.setQuantidade(form.getQuantidade());
        anuncio.setUnidade(form.getUnidade());
        anuncio.setModalidade(form.getModalidade());
        // "Desejo receber em troca" só faz sentido em anúncios de troca
        anuncio.setDesejaReceber(form.getModalidade() == Modalidade.TROCA
                ? textoOuNulo(form.getDesejaReceber()) : null);
        anuncio.setLocalRetirada(form.getLocalRetirada().trim());
        anuncio.setDisponivelAte(form.getDisponivelAte());
        anuncio.setStatus(StatusAnuncio.ATIVO);
        anuncio.setDataCriacao(LocalDateTime.now());
        anuncio.setUsuario(anunciante);
        anuncio.setFotoUrl(fotoService.salvar(foto));

        return anuncios.save(anuncio);
    }

    /**
     * Remove o próprio anúncio. Só é permitido enquanto ATIVO:
     * depois de RESERVADO já existe um acordo com outra pessoa.
     * As propostas (pendentes/rejeitadas) são removidas junto (CascadeType.REMOVE).
     */
    @Transactional
    public void excluir(Integer anuncioId, Usuario usuario) {
        Anuncio anuncio = buscarPorId(anuncioId);

        if (!Objects.equals(anuncio.getUsuario().getId(), usuario.getId())) {
            throw new RegraNegocioException("Você só pode excluir os seus próprios anúncios.");
        }
        if (anuncio.getStatus() != StatusAnuncio.ATIVO) {
            throw new RegraNegocioException("Anúncios reservados ou concluídos não podem ser excluídos.");
        }

        String fotoUrl = anuncio.getFotoUrl();
        anuncios.delete(anuncio);
        fotoService.remover(fotoUrl);
    }

    private static String textoOuNulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
