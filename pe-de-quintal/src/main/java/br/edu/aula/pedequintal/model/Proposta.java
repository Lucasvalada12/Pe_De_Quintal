package br.edu.aula.pedequintal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "proposta")
public class Proposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "item_oferecido")
    private String itemOferecido;

    private String mensagem;

    @Enumerated(EnumType.STRING)
    private StatusProposta status;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "data_resposta")
    private LocalDateTime dataResposta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "anuncio_id")
    private Anuncio anuncio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proponente_id")
    private Usuario proponente;       // quem enviou a proposta

    public Proposta() {}

    public boolean isPendente() { return status == StatusProposta.PENDENTE; }

    public boolean isAceita() { return status == StatusProposta.ACEITA; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getItemOferecido() { return itemOferecido; }
    public void setItemOferecido(String itemOferecido) { this.itemOferecido = itemOferecido; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public StatusProposta getStatus() { return status; }
    public void setStatus(StatusProposta status) { this.status = status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public LocalDateTime getDataResposta() { return dataResposta; }
    public void setDataResposta(LocalDateTime dataResposta) { this.dataResposta = dataResposta; }
    public Anuncio getAnuncio() { return anuncio; }
    public void setAnuncio(Anuncio anuncio) { this.anuncio = anuncio; }
    public Usuario getProponente() { return proponente; }
    public void setProponente(Usuario proponente) { this.proponente = proponente; }
}
