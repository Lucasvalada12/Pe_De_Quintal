package br.edu.aula.pedequintal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registro de uma entrega concluída (gerado ao clicar em "Confirmar entrega"). */
@Entity
@Table(name = "transacao")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @Column(name = "peso_kg")
    private BigDecimal pesoKg;        // null quando a unidade não é kg/g

    @OneToOne(optional = false)
    @JoinColumn(name = "proposta_id")
    private Proposta proposta;

    public Transacao() {}

    public Transacao(Proposta proposta, LocalDateTime dataConclusao, BigDecimal pesoKg) {
        this.proposta = proposta;
        this.dataConclusao = dataConclusao;
        this.pesoKg = pesoKg;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDateTime dataConclusao) { this.dataConclusao = dataConclusao; }
    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }
    public Proposta getProposta() { return proposta; }
    public void setProposta(Proposta proposta) { this.proposta = proposta; }
}
