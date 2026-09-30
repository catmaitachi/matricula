package br.edu.matricula.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nome;

    private int numCreditos;

    protected Curso() {
    }

    public Curso(String nome, int numCreditos) {
        alterar(nome, numCreditos);
    }

    public void alterar(String nome, int numCreditos) {
        if (numCreditos < 1) {
            throw new RegraDeNegocioException("DADO_INVALIDO", "O número de créditos deve ser positivo.");
        }
        this.nome = Validacao.texto(nome, 120, "Nome do curso");
        this.numCreditos = numCreditos;
    }

    public Long id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public int numCreditos() {
        return numCreditos;
    }
}
