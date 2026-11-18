package br.edu.aula.pedequintal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados do formulário "Criar conta" (W1).
 * Usamos uma classe separada da entidade para não expor senhaHash no formulário.
 */
public class CadastroForm {

    @NotBlank(message = "Informe seu nome")
    @Size(max = 100, message = "Nome muito longo")
    private String nome;

    @NotBlank(message = "Informe seu e-mail")
    @Email(message = "E-mail inválido")
    @Size(max = 150, message = "E-mail muito longo")
    private String email;

    @NotBlank(message = "Informe seu telefone")
    @Pattern(regexp = "^[0-9()\\s+-]{10,20}$", message = "Telefone inválido. Ex.: (31) 99999-9999")
    private String telefone;

    @NotBlank(message = "Crie uma senha")
    @Size(min = 6, max = 60, message = "A senha precisa ter entre 6 e 60 caracteres")
    private String senha;

    @NotBlank(message = "Informe sua cidade")
    @Size(max = 100, message = "Cidade muito longa")
    private String cidade;

    @NotBlank(message = "Informe a UF")
    @Pattern(regexp = "^[A-Za-z]{2}$", message = "UF deve ter 2 letras. Ex.: MG")
    private String uf;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
}
