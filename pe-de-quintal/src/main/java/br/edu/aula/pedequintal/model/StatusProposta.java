package br.edu.aula.pedequintal.model;

public enum StatusProposta {
    PENDENTE("Pendente"),
    ACEITA("Aceita"),
    REJEITADA("Rejeitada");

    private final String descricao;

    StatusProposta(String descricao) { this.descricao = descricao; }

    public String getDescricao() { return descricao; }
}
