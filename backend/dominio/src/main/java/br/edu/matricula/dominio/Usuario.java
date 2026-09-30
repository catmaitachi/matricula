package br.edu.matricula.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/** Quem entra no sistema. A senha nunca é guardada: só o hash (gerado pela camada de segurança). */
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "papel", length = 12)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 30)
    private String numPessoa;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 100)
    private String senhaHash;

    private boolean ativo = true;

    protected Usuario() {
    }

    protected Usuario(String numPessoa, String nome, String senhaHash) {
        this.numPessoa = Validacao.texto(numPessoa, 30, "Nº de pessoa");
        this.nome = Validacao.texto(nome, 120, "Nome");
        this.senhaHash = Validacao.texto(senhaHash, 100, "Senha");
    }

    public abstract Papel papel();

    public Long id() {
        return id;
    }

    public String numPessoa() {
        return numPessoa;
    }

    public String nome() {
        return nome;
    }

    public String senhaHash() {
        return senhaHash;
    }

    public boolean ativo() {
        return ativo;
    }

    public void alterarNome(String nome) {
        this.nome = Validacao.texto(nome, 120, "Nome");
    }

    public void alterarSenhaHash(String senhaHash) {
        this.senhaHash = Validacao.texto(senhaHash, 100, "Senha");
    }

    public void ativo(boolean ativo) {
        this.ativo = ativo;
    }
}
