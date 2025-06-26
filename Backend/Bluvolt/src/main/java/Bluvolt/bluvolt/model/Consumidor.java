package Bluvolt.bluvolt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;


@Entity
public class Consumidor{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String tipo = "consumidor";

    @NotEmpty
    private String nome;

    @NotEmpty
    private String email;

    @NotEmpty
    private String senha;

    @NotEmpty
    private String cpf;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "consumidor_favoritos",
            joinColumns = @JoinColumn(name = "consumidor_id"),
            inverseJoinColumns = @JoinColumn(name = "produto_id"))
    private List<Produto> favoritos = new ArrayList<>();

    public List<Produto> getFavoritos() {
        return favoritos;
    }

    public void adicionarFavorito(Produto produto) {
        if (!favoritos.contains(produto)) {
            favoritos.add(produto);
        }
    }

    public void removerFavorito(Produto produto) {
        favoritos.remove(produto);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }


}
