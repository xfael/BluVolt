package Bluvolt.bluvolt.model;

import jakarta.persistence.*;

@Entity
public class Favorito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Consumidor consumidor;

    @ManyToOne
    private Produto produto;

    public Long getId() { return id; }

    public Consumidor getConsumidor() { return consumidor; }
    public void setConsumidor(Consumidor consumidor) { this.consumidor = consumidor; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
}

