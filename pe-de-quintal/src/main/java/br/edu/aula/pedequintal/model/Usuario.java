package br.edu.aula.pedequintal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    private String email;

    @Column(name = "senha_hash")
    private String senhaHash;   // nunca guardamos a senha pura, só o hash BCrypt

    private String telefone;

    private String cidade;

    private String uf;

    public Usuario() {}

    public Usuario(String nome, String email, String senhaHash, String telefone, String cidade, String uf) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.telefone = telefone;
        this.cidade = cidade;
        this.uf = uf;
    }

    /** "Maria Silva" -> "Maria S." (como nos protótipos) */
    public String getNomeCurto() {
        if (nome == null || nome.isBlank()) return "";
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) return partes[0];
        return partes[0] + " " + partes[partes.length - 1].charAt(0) + ".";
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
}
