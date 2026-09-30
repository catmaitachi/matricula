package br.edu.matricula.dominio;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PROFESSOR")
public class Professor extends Usuario {

    protected Professor() {
    }

    public Professor(String numPessoa, String nome, String senhaHash) {
        super(numPessoa, nome, senhaHash);
    }

    @Override
    public Papel papel() {
        return Papel.PROFESSOR;
    }
}
