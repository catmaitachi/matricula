package br.edu.matricula.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.LinkedHashSet;
import java.util.Set;

/** Disciplina com os limites de alunos que valem para cada uma de suas turmas (padrão: mínimo 3, máximo 60). */
@Entity
public class Disciplina {

    public static final int MINIMO_PADRAO = 3;
    public static final int MAXIMO_PADRAO = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nome;

    private boolean ativa = true;
    private int minAlunos = MINIMO_PADRAO;
    private int maxAlunos = MAXIMO_PADRAO;

    @ManyToMany
    private Set<Curso> cursos = new LinkedHashSet<>();

    protected Disciplina() {
    }

    public Disciplina(String nome, int minAlunos, int maxAlunos, Set<Curso> cursos) {
        alterar(nome, minAlunos, maxAlunos, cursos);
    }

    public void alterar(String nome, int minAlunos, int maxAlunos, Set<Curso> cursos) {
        if (minAlunos < 1 || maxAlunos < minAlunos) {
            throw new RegraDeNegocioException("LIMITES_INVALIDOS",
                    "O mínimo de alunos deve ser pelo menos 1 e não pode passar do máximo.");
        }
        this.nome = Validacao.texto(nome, 120, "Nome da disciplina");
        this.minAlunos = minAlunos;
        this.maxAlunos = maxAlunos;
        this.cursos = new LinkedHashSet<>(cursos);
    }

    /** Compara por id pelo método (não pelo campo): quem chega aqui pode ser um proxy do Hibernate, cujo campo é nulo. */
    public boolean mesma(Disciplina outra) {
        return this == outra || (id() != null && id().equals(outra.id()));
    }

    public Long id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public boolean ativa() {
        return ativa;
    }

    public void ativa(boolean ativa) {
        this.ativa = ativa;
    }

    public int minAlunos() {
        return minAlunos;
    }

    public int maxAlunos() {
        return maxAlunos;
    }

    public Set<Curso> cursos() {
        return Set.copyOf(cursos);
    }
}
