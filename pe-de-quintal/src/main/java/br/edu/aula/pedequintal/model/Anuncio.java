package br.edu.aula.pedequintal.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "anuncio")
public class Anuncio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String titulo;

    private String descricao;

    private BigDecimal quantidade;

    private String unidade;

    @Enumerated(EnumType.STRING)   // grava "DOACAO"/"TROCA" em vez de 0/1
    private Modalidade modalidade;

    @Column(name = "deseja_receber")
    private String desejaReceber;

    @Enumerated(EnumType.STRING)
    private StatusAnuncio status;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "local_retirada")
    private String localRetirada;

    @Column(name = "disponivel_ate")
    private LocalDate disponivelAte;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    // Optimistic locking: impede que dois "Aceitar" simultâneos reservem o mesmo item
    @Version
    private Integer versao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;          // o anunciante

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // 1:N Proposta. Ao excluir o anúncio, as propostas dele também são excluídas.
    @OneToMany(mappedBy = "anuncio", cascade = CascadeType.REMOVE)
    @OrderBy("dataCriacao DESC")
    private List<Proposta> propostas = new ArrayList<>();

    public Anuncio() {}

    public boolean isAtivo() { return status == StatusAnuncio.ATIVO; }

    public boolean isTroca() { return modalidade == Modalidade.TROCA; }

    /** Quantidade sem zeros desnecessários: 3.00 -> "3", 0.50 -> "0.5" */
    public String getQuantidadeFormatada() {
        if (quantidade == null) return "";
        return quantidade.stripTrailingZeros().toPlainString();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
    public Modalidade getModalidade() { return modalidade; }
    public void setModalidade(Modalidade modalidade) { this.modalidade = modalidade; }
    public String getDesejaReceber() { return desejaReceber; }
    public void setDesejaReceber(String desejaReceber) { this.desejaReceber = desejaReceber; }
    public StatusAnuncio getStatus() { return status; }
    public void setStatus(StatusAnuncio status) { this.status = status; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public String getLocalRetirada() { return localRetirada; }
    public void setLocalRetirada(String localRetirada) { this.localRetirada = localRetirada; }
    public LocalDate getDisponivelAte() { return disponivelAte; }
    public void setDisponivelAte(LocalDate disponivelAte) { this.disponivelAte = disponivelAte; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public Integer getVersao() { return versao; }
    public void setVersao(Integer versao) { this.versao = versao; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public List<Proposta> getPropostas() { return propostas; }
    public void setPropostas(List<Proposta> propostas) { this.propostas = propostas; }
}
