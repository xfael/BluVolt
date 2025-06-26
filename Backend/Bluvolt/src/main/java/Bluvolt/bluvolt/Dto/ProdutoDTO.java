package Bluvolt.bluvolt.Dto;

import Bluvolt.bluvolt.model.Produto;

public class ProdutoDTO {

    private Long id;
    private String nome;
    private String descricao;
    private String tipoEnergia;
    private String imagemUrl;
    private double preco;
    private double desconto;
    private int estoque;
    private boolean favoritado;

    public static ProdutoDTO from(Produto produto) {
        ProdutoDTO dto = new ProdutoDTO();
        dto.setId(produto.getId());
        dto.setNome(produto.getNome());
        dto.setDescricao(produto.getDescricao());
        dto.setImagemUrl(produto.getImagemUrl());
        dto.setPreco(produto.getPreco());
        dto.setDesconto(produto.getDesconto());
        dto.setEstoque(produto.getEstoque());
        return dto;
    }

    public void setFavoritado(boolean favoritado) {
        this.favoritado = favoritado;
    }

    public boolean isFavoritado() {
        return favoritado;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipoEnergia() {
        return tipoEnergia;
    }

    public void setTipoEnergia(String tipoEnergia) {
        this.tipoEnergia = tipoEnergia;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public double getDesconto() {
        return desconto;
    }

    public void setDesconto(double desconto) {
        this.desconto = desconto;
    }

    public int getEstoque() {
        return estoque;
    }

    public void setEstoque(int estoque) {
        this.estoque = estoque;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
