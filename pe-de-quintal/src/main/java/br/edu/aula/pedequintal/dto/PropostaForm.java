package br.edu.aula.pedequintal.dto;

import jakarta.validation.constraints.Size;

/** Formulário "Enviar proposta" na tela de detalhe do item. */
public class PropostaForm {

    // Obrigatório apenas para anúncios de TROCA (regra conferida no PropostaService)
    @Size(max = 200, message = "Texto muito longo (máx. 200)")
    private String itemOferecido;

    @Size(max = 500, message = "Mensagem muito longa (máx. 500)")
    private String mensagem;

    public String getItemOferecido() { return itemOferecido; }
    public void setItemOferecido(String itemOferecido) { this.itemOferecido = itemOferecido; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
