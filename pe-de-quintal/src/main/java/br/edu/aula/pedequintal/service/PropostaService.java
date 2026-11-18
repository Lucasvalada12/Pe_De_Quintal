package br.edu.aula.pedequintal.service;

import br.edu.aula.pedequintal.dto.PropostaForm;
import br.edu.aula.pedequintal.model.Anuncio;
import br.edu.aula.pedequintal.model.Proposta;
import br.edu.aula.pedequintal.model.StatusAnuncio;
import br.edu.aula.pedequintal.model.StatusProposta;
import br.edu.aula.pedequintal.model.Transacao;
import br.edu.aula.pedequintal.model.Usuario;
import br.edu.aula.pedequintal.repository.AnuncioRepository;
import br.edu.aula.pedequintal.repository.PropostaRepository;
import br.edu.aula.pedequintal.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Máquina de estados da proposta — o coração do sistema (tela W4).
 *
 *   PENDENTE --aceitar--> ACEITA --concluir--> (Transacao criada, anúncio CONCLUIDO)
 *   PENDENTE --rejeitar-> REJEITADA
 */
@Service
public class PropostaService {

    private final PropostaRepository propostas;
    private final AnuncioRepository anuncios;
    private final TransacaoRepository transacoes;

    public PropostaService(PropostaRepository propostas, AnuncioRepository anuncios, TransacaoRepository transacoes) {
        this.propostas = propostas;
        this.anuncios = anuncios;
        this.transacoes = transacoes;
    }

    // ------------------------------------------------------------------ envio

    @Transactional
    public Proposta enviar(Integer anuncioId, PropostaForm form, Usuario proponente) {
        Anuncio anuncio = anuncios.findById(anuncioId)
                .orElseThrow(() -> new RegraNegocioException("Anúncio não encontrado."));

        if (Objects.equals(anuncio.getUsuario().getId(), proponente.getId())) {
            throw new RegraNegocioException("Você não pode enviar proposta para o seu próprio anúncio.");
        }
        if (anuncio.getStatus() != StatusAnuncio.ATIVO
                || anuncio.getDisponivelAte().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("Este item não está mais disponível.");
        }
        if (propostas.existsByAnuncioIdAndProponenteIdAndStatus(anuncioId, proponente.getId(), StatusProposta.PENDENTE)) {
            throw new RegraNegocioException("Você já tem uma proposta pendente para este item.");
        }

        String itemOferecido = textoOuNulo(form.getItemOferecido());
        if (anuncio.isTroca() && itemOferecido == null) {
            throw new RegraNegocioException("Informe o que você oferece em troca.");
        }

        Proposta proposta = new Proposta();
        proposta.setAnuncio(anuncio);
        proposta.setProponente(proponente);
        proposta.setItemOferecido(itemOferecido);
        proposta.setMensagem(textoOuNulo(form.getMensagem()));
        proposta.setStatus(StatusProposta.PENDENTE);
        proposta.setDataCriacao(LocalDateTime.now());
        return propostas.save(proposta);
    }

    // --------------------------------------------------------------- listagem

    public List<Proposta> listarRecebidas(Usuario usuario) {
        return propostas.findByAnuncioUsuarioIdOrderByDataCriacaoDesc(usuario.getId());
    }

    public List<Proposta> listarEnviadas(Usuario usuario) {
        return propostas.findByProponenteIdOrderByDataCriacaoDesc(usuario.getId());
    }

    public long contarRecebidasPendentes(Usuario usuario) {
        return propostas.countByAnuncioUsuarioIdAndStatus(usuario.getId(), StatusProposta.PENDENTE);
    }

    public boolean temPropostaPendente(Integer anuncioId, Usuario usuario) {
        return propostas.existsByAnuncioIdAndProponenteIdAndStatus(anuncioId, usuario.getId(), StatusProposta.PENDENTE);
    }

    /** Privacidade/LGPD: o telefone do anunciante só aparece para quem teve a proposta aceita. */
    public boolean temPropostaAceita(Integer anuncioId, Usuario usuario) {
        return propostas.existsByAnuncioIdAndProponenteIdAndStatus(anuncioId, usuario.getId(), StatusProposta.ACEITA);
    }

    // ---------------------------------------------------- aceite / rejeição

    /**
     * Transação atômica: tudo acontece ou nada acontece.
     * 1) proposta -> ACEITA
     * 2) anúncio  -> RESERVADO   (@Version no Anuncio barra dois aceites simultâneos)
     * 3) demais propostas PENDENTES do mesmo item -> REJEITADA
     * Só depois disso o telefone das duas partes passa a ser exibido.
     */
    @Transactional
    public Proposta aceitar(Integer propostaId, Usuario usuario) {
        Proposta proposta = buscarComoDonoDoAnuncio(propostaId, usuario);
        Anuncio anuncio = proposta.getAnuncio();

        if (proposta.getStatus() != StatusProposta.PENDENTE) {
            throw new RegraNegocioException("Esta proposta já foi respondida.");
        }
        if (anuncio.getStatus() != StatusAnuncio.ATIVO) {
            throw new RegraNegocioException("Este anúncio já está reservado ou concluído.");
        }

        LocalDateTime agora = LocalDateTime.now();

        proposta.setStatus(StatusProposta.ACEITA);
        proposta.setDataResposta(agora);

        anuncio.setStatus(StatusAnuncio.RESERVADO);
        anuncios.save(anuncio);

        for (Proposta outra : propostas.findByAnuncioIdAndStatus(anuncio.getId(), StatusProposta.PENDENTE)) {
            if (!outra.getId().equals(proposta.getId())) {
                outra.setStatus(StatusProposta.REJEITADA);
                outra.setDataResposta(agora);
                propostas.save(outra);
            }
        }

        return propostas.save(proposta);
    }

    @Transactional
    public Proposta rejeitar(Integer propostaId, Usuario usuario) {
        Proposta proposta = buscarComoDonoDoAnuncio(propostaId, usuario);

        if (proposta.getStatus() != StatusProposta.PENDENTE) {
            throw new RegraNegocioException("Esta proposta já foi respondida.");
        }

        proposta.setStatus(StatusProposta.REJEITADA);
        proposta.setDataResposta(LocalDateTime.now());
        return propostas.save(proposta);
    }

    // -------------------------------------------------------------- conclusão

    /**
     * "Confirmar entrega": qualquer uma das duas partes pode confirmar.
     * Gera a Transacao (1:1 com a proposta) e encerra o anúncio.
     */
    @Transactional
    public Transacao concluir(Integer propostaId, Usuario usuario) {
        Proposta proposta = propostas.findById(propostaId)
                .orElseThrow(() -> new RegraNegocioException("Proposta não encontrada."));
        Anuncio anuncio = proposta.getAnuncio();

        boolean ehDono = Objects.equals(anuncio.getUsuario().getId(), usuario.getId());
        boolean ehProponente = Objects.equals(proposta.getProponente().getId(), usuario.getId());
        if (!ehDono && !ehProponente) {
            throw new RegraNegocioException("Você não participa desta proposta.");
        }
        if (proposta.getStatus() != StatusProposta.ACEITA || anuncio.getStatus() != StatusAnuncio.RESERVADO) {
            throw new RegraNegocioException("Só é possível confirmar a entrega de uma proposta aceita.");
        }
        if (transacoes.existsByPropostaId(propostaId)) {
            throw new RegraNegocioException("Esta entrega já foi confirmada.");
        }

        anuncio.setStatus(StatusAnuncio.CONCLUIDO);
        anuncios.save(anuncio);

        Transacao transacao = new Transacao(proposta, LocalDateTime.now(), calcularPesoKg(anuncio));
        return transacoes.save(transacao);
    }

    /** kg -> mesmo valor; g -> divide por 1000; outras unidades -> null (sem estimativa). */
    static BigDecimal calcularPesoKg(Anuncio anuncio) {
        if (anuncio.getQuantidade() == null || anuncio.getUnidade() == null) return null;
        return switch (anuncio.getUnidade()) {
            case "kg" -> anuncio.getQuantidade();
            case "g" -> anuncio.getQuantidade().divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            default -> null;
        };
    }

    // ---------------------------------------------------------------- apoio

    private Proposta buscarComoDonoDoAnuncio(Integer propostaId, Usuario usuario) {
        Proposta proposta = propostas.findById(propostaId)
                .orElseThrow(() -> new RegraNegocioException("Proposta não encontrada."));
        if (!Objects.equals(proposta.getAnuncio().getUsuario().getId(), usuario.getId())) {
            throw new RegraNegocioException("Apenas o dono do anúncio pode responder a esta proposta.");
        }
        return proposta;
    }

    private static String textoOuNulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
