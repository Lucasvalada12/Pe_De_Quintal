package br.edu.aula.pedequintal.model;

public enum Modalidade {
    DOACAO("Doação"),
    TROCA("Troca");

    private final String descricao;

    Modalidade(String descricao) { this.descricao = descricao; }

    public String getDescricao() { return descricao; }
}
