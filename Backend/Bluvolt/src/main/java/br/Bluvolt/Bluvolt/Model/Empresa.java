package br.Bluvolt.Bluvolt.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Empresa extends Usuario {

    @Column(nullable = false)
    private String nomeEmpresa;

    @Column(nullable = false, unique = true)
    private String cnpj;

    @Column(nullable = false)
    private String tipoEnergia;
}
