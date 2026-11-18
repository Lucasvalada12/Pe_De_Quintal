package br.edu.aula.pedequintal.model;

public enum StatusAnuncio {
    ATIVO("Ativo"),
    RESERVADO("Reservado"),
    CONCLUIDO("Concluído");

    private final String descricao;

    StatusAnuncio(String descricao) { this.descricao = descricao; }

    public String getDescricao() { return descricao; }
}
