package br.edu.aula.pedequintal.dto;

import br.edu.aula.pedequintal.model.Modalidade;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Dados do formulário "Publicar item" (W3). A foto chega separada, como MultipartFile. */
public class AnuncioForm {

    @NotBlank(message = "Informe o título do item")
    @Size(max = 100, message = "Título muito longo (máx. 100)")
    private String titulo;

    @Size(max = 500, message = "Descrição muito longa (máx. 500)")
    private String descricao;

    @NotNull(message = "Escolha a categoria")
    private Integer categoriaId;

    @NotNull(message = "Informe a quantidade")
    @DecimalMin(value = "0.01", message = "A quantidade deve ser maior que zero")
    @DecimalMax(value = "99999", message = "Quantidade muito alta")
    private BigDecimal quantidade;

    @NotBlank(message = "Escolha a unidade")
    private String unidade;

    @NotNull(message = "Escolha a modalidade")
    private Modalidade modalidade = Modalidade.DOACAO;

    @Size(max = 200, message = "Texto muito longo (máx. 200)")
    private String desejaReceber;

    @NotBlank(message = "Informe o local de retirada")
    @Size(max = 150, message = "Local muito longo (máx. 150)")
    private String localRetirada;

    @NotNull(message = "Informe até quando o item está disponível")
    @FutureOrPresent(message = "A data não pode estar no passado")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate disponivelAte;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
    public Modalidade getModalidade() { return modalidade; }
    public void setModalidade(Modalidade modalidade) { this.modalidade = modalidade; }
    public String getDesejaReceber() { return desejaReceber; }
    public void setDesejaReceber(String desejaReceber) { this.desejaReceber = desejaReceber; }
    public String getLocalRetirada() { return localRetirada; }
    public void setLocalRetirada(String localRetirada) { this.localRetirada = localRetirada; }
    public LocalDate getDisponivelAte() { return disponivelAte; }
    public void setDisponivelAte(LocalDate disponivelAte) { this.disponivelAte = disponivelAte; }
}
