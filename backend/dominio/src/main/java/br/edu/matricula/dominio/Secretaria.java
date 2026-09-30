package br.edu.matricula.dominio;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SECRETARIA")
public class Secretaria extends Usuario {

    protected Secretaria() {
    }

    public Secretaria(String numPessoa, String nome, String senhaHash) {
        super(numPessoa, nome, senhaHash);
    }

    @Override
    public Papel papel() {
        return Papel.SECRETARIA;
    }
}
