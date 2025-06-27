package Bluvolt.bluvolt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String tipo = "empresa";

    @NotEmpty
    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    @NotEmpty
    private String senha;

    @NotEmpty
    private String tipoEnergia;

    @NotEmpty
    private String cnpj;

    private Double totalVendasMensal;
    private Integer totalPedidos = 0;
    private Integer totalProdutos = 0;
    private Integer totalClientes = 0;


    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    private List<Pedido> pedidos;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    private List<Produto> produtos;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    private List<Consumidor> consumidores;

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getTipoEnergia() {
        return tipoEnergia;
    }

    public void setTipoEnergia(String tipoEnergia) {
        this.tipoEnergia = tipoEnergia;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public List<Consumidor> getConsumidores() {
        return consumidores;
    }

    public void setConsumidores(List<Consumidor> consumidores) {
        this.consumidores = consumidores;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Produto> produtos) {
        this.produtos = produtos;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    public Integer getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(Integer totalClientes) {
        this.totalClientes = totalClientes;
    }

    public Integer getTotalProdutos() {
        return totalProdutos;
    }

    public void setTotalProdutos(Integer totalProdutos) {
        this.totalProdutos = totalProdutos;
    }

    public Integer getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(Integer totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public Double getTotalVendasMensal() {
        return totalVendasMensal;
    }

    public void setTotalVendasMensal(Double totalVendasMensal) {
        this.totalVendasMensal = totalVendasMensal;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
