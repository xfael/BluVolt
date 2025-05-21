package br.Bluvolt.Bluvolt.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Consumidor extends Usuario {

    @Column(nullable = false)
    private String nomeCompleto;

    @Column(nullable = false, unique = true)
    private String cpf;
}
