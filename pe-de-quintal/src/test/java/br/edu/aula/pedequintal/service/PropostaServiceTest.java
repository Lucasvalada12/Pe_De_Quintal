package br.edu.aula.pedequintal.service;

import br.edu.aula.pedequintal.dto.PropostaForm;
import br.edu.aula.pedequintal.model.Anuncio;
import br.edu.aula.pedequintal.model.Modalidade;
import br.edu.aula.pedequintal.model.Proposta;
import br.edu.aula.pedequintal.model.StatusAnuncio;
import br.edu.aula.pedequintal.model.StatusProposta;
import br.edu.aula.pedequintal.model.Transacao;
import br.edu.aula.pedequintal.model.Usuario;
import br.edu.aula.pedequintal.repository.AnuncioRepository;
import br.edu.aula.pedequintal.repository.PropostaRepository;
import br.edu.aula.pedequintal.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropostaServiceTest {

    @Mock PropostaRepository propostas;
    @Mock AnuncioRepository anuncios;
    @Mock TransacaoRepository transacoes;

    @InjectMocks PropostaService service;

    Usuario dono;
    Usuario ana;
    Usuario celia;
    Anuncio limao;

    @BeforeEach
    void setUp() {
        dono = usuario(1, "João Pereira");
        ana = usuario(2, "Ana Lima");
        celia = usuario(3, "Célia Rocha");

        limao = new Anuncio();
        limao.setId(10);
        limao.setTitulo("Limão taiti");
        limao.setQuantidade(new BigDecimal("3"));
        limao.setUnidade("kg");
        limao.setModalidade(Modalidade.TROCA);
        limao.setStatus(StatusAnuncio.ATIVO);
        limao.setDisponivelAte(LocalDate.now().plusDays(5));
        limao.setUsuario(dono);
    }

    // ------------------------------------------------------------ aceitar

    @Test
    @DisplayName("Aceitar: proposta ACEITA, anúncio RESERVADO e demais pendentes REJEITADAS")
    void aceitarExecutaTransacaoCompleta() {
        Proposta daAna = proposta(100, ana, StatusProposta.PENDENTE);
        Proposta daCelia = proposta(101, celia, StatusProposta.PENDENTE);

        when(propostas.findById(100)).thenReturn(Optional.of(daAna));
        when(propostas.findByAnuncioIdAndStatus(10, StatusProposta.PENDENTE)).thenReturn(List.of(daAna, daCelia));
        when(propostas.save(any(Proposta.class))).thenAnswer(inv -> inv.getArgument(0));

        Proposta aceita = service.aceitar(100, dono);

        assertThat(aceita.getStatus()).isEqualTo(StatusProposta.ACEITA);
        assertThat(aceita.getDataResposta()).isNotNull();
        assertThat(limao.getStatus()).isEqualTo(StatusAnuncio.RESERVADO);
        assertThat(daCelia.getStatus()).isEqualTo(StatusProposta.REJEITADA);
        assertThat(daCelia.getDataResposta()).isNotNull();
        verify(anuncios).save(limao);
    }

    @Test
    @DisplayName("Aceitar: só o dono do anúncio pode aceitar")
    void aceitarPorOutroUsuarioFalha() {
        when(propostas.findById(100)).thenReturn(Optional.of(proposta(100, ana, StatusProposta.PENDENTE)));

        assertThatThrownBy(() -> service.aceitar(100, celia))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("dono do anúncio");
        assertThat(limao.getStatus()).isEqualTo(StatusAnuncio.ATIVO);
    }

    @Test
    @DisplayName("Aceitar: anúncio já reservado não aceita outra proposta")
    void aceitarComAnuncioReservadoFalha() {
        limao.setStatus(StatusAnuncio.RESERVADO);
        when(propostas.findById(100)).thenReturn(Optional.of(proposta(100, ana, StatusProposta.PENDENTE)));

        assertThatThrownBy(() -> service.aceitar(100, dono))
                .isInstanceOf(RegraNegocioException.class);
        verify(anuncios, never()).save(any());
    }

    @Test
    @DisplayName("Aceitar: proposta já respondida não pode ser aceita")
    void aceitarPropostaJaRespondidaFalha() {
        when(propostas.findById(100)).thenReturn(Optional.of(proposta(100, ana, StatusProposta.REJEITADA)));

        assertThatThrownBy(() -> service.aceitar(100, dono))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já foi respondida");
    }

    // ----------------------------------------------------------- rejeitar

    @Test
    @DisplayName("Rejeitar: proposta vira REJEITADA e o anúncio continua ATIVO")
    void rejeitarMantemAnuncioAtivo() {
        Proposta daAna = proposta(100, ana, StatusProposta.PENDENTE);
        when(propostas.findById(100)).thenReturn(Optional.of(daAna));
        when(propostas.save(any(Proposta.class))).thenAnswer(inv -> inv.getArgument(0));

        service.rejeitar(100, dono);

        assertThat(daAna.getStatus()).isEqualTo(StatusProposta.REJEITADA);
        assertThat(limao.getStatus()).isEqualTo(StatusAnuncio.ATIVO);
    }

    @Test
    @DisplayName("Rejeitar: só o dono do anúncio pode rejeitar")
    void rejeitarPorOutroUsuarioFalha() {
        when(propostas.findById(100)).thenReturn(Optional.of(proposta(100, ana, StatusProposta.PENDENTE)));

        assertThatThrownBy(() -> service.rejeitar(100, ana))
                .isInstanceOf(RegraNegocioException.class);
    }

    // ------------------------------------------------------------- enviar

    @Test
    @DisplayName("Enviar: cria proposta PENDENTE")
    void enviarCriaPropostaPendente() {
        when(anuncios.findById(10)).thenReturn(Optional.of(limao));
        when(propostas.save(any(Proposta.class))).thenAnswer(inv -> inv.getArgument(0));

        Proposta p = service.enviar(10, form("cenoura orgânica 1 kg"), ana);

        assertThat(p.getStatus()).isEqualTo(StatusProposta.PENDENTE);
        assertThat(p.getProponente()).isSameAs(ana);
        assertThat(p.getItemOferecido()).isEqualTo("cenoura orgânica 1 kg");
    }

    @Test
    @DisplayName("Enviar: não pode propor para o próprio anúncio")
    void enviarParaProprioAnuncioFalha() {
        when(anuncios.findById(10)).thenReturn(Optional.of(limao));

        assertThatThrownBy(() -> service.enviar(10, form("algo"), dono))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("próprio anúncio");
    }

    @Test
    @DisplayName("Enviar: anúncio de troca exige o item oferecido")
    void enviarTrocaSemItemFalha() {
        when(anuncios.findById(10)).thenReturn(Optional.of(limao));

        assertThatThrownBy(() -> service.enviar(10, form("  "), ana))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("oferece em troca");
    }

    @Test
    @DisplayName("Enviar: anúncio reservado não recebe propostas")
    void enviarParaAnuncioReservadoFalha() {
        limao.setStatus(StatusAnuncio.RESERVADO);
        when(anuncios.findById(10)).thenReturn(Optional.of(limao));

        assertThatThrownBy(() -> service.enviar(10, form("algo"), ana))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não está mais disponível");
    }

    @Test
    @DisplayName("Enviar: não permite duas propostas pendentes do mesmo usuário")
    void enviarDuplicadaFalha() {
        when(anuncios.findById(10)).thenReturn(Optional.of(limao));
        when(propostas.existsByAnuncioIdAndProponenteIdAndStatus(10, 2, StatusProposta.PENDENTE)).thenReturn(true);

        assertThatThrownBy(() -> service.enviar(10, form("algo"), ana))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já tem uma proposta");
    }

    // ----------------------------------------------------------- concluir

    @Test
    @DisplayName("Concluir: gera Transacao com peso em kg e encerra o anúncio")
    void concluirGeraTransacao() {
        limao.setStatus(StatusAnuncio.RESERVADO);
        Proposta aceita = proposta(100, ana, StatusProposta.ACEITA);
        when(propostas.findById(100)).thenReturn(Optional.of(aceita));
        when(transacoes.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

        Transacao t = service.concluir(100, ana);   // o proponente também pode confirmar

        assertThat(limao.getStatus()).isEqualTo(StatusAnuncio.CONCLUIDO);
        assertThat(t.getProposta()).isSameAs(aceita);
        assertThat(t.getPesoKg()).isEqualByComparingTo("3");
        assertThat(t.getDataConclusao()).isNotNull();
    }

    @Test
    @DisplayName("Concluir: terceiros não podem confirmar a entrega")
    void concluirPorTerceiroFalha() {
        limao.setStatus(StatusAnuncio.RESERVADO);
        when(propostas.findById(100)).thenReturn(Optional.of(proposta(100, ana, StatusProposta.ACEITA)));

        assertThatThrownBy(() -> service.concluir(100, celia))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Concluir: proposta pendente não pode ser concluída")
    void concluirPropostaPendenteFalha() {
        when(propostas.findById(100)).thenReturn(Optional.of(proposta(100, ana, StatusProposta.PENDENTE)));

        assertThatThrownBy(() -> service.concluir(100, dono))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Peso: gramas são convertidas para kg; outras unidades ficam sem peso")
    void calculoDePeso() {
        limao.setUnidade("g");
        limao.setQuantidade(new BigDecimal("800"));
        assertThat(PropostaService.calcularPesoKg(limao)).isEqualByComparingTo("0.8");

        limao.setUnidade("maço");
        assertThat(PropostaService.calcularPesoKg(limao)).isNull();
    }

    // -------------------------------------------------------------- apoio

    private Usuario usuario(int id, String nome) {
        Usuario u = new Usuario(nome, nome.split(" ")[0].toLowerCase() + "@teste.com", "hash",
                "(31) 99999-000" + id, "Sete Lagoas", "MG");
        u.setId(id);
        return u;
    }

    private Proposta proposta(int id, Usuario proponente, StatusProposta status) {
        Proposta p = new Proposta();
        p.setId(id);
        p.setAnuncio(limao);
        p.setProponente(proponente);
        p.setItemOferecido("algo");
        p.setStatus(status);
        p.setDataCriacao(LocalDateTime.now());
        return p;
    }

    private PropostaForm form(String itemOferecido) {
        PropostaForm f = new PropostaForm();
        f.setItemOferecido(itemOferecido);
        return f;
    }
}
