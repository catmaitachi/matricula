package br.edu.matricula.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/** Uma turma de uma disciplina num semestre. Os limites de alunos vêm da disciplina. */
@Entity
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Curriculo curriculo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Disciplina disciplina;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Professor professor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Turno turno;

    @Column(nullable = false, length = 1)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoTurma estado = EstadoTurma.ABERTA;

    @OneToMany(mappedBy = "turma")
    private final List<Matricula> matriculas = new ArrayList<>();

    protected Turma() {
    }

    Turma(Curriculo curriculo, Disciplina disciplina, Professor professor, Turno turno, String codigo) {
        this.curriculo = curriculo;
        this.disciplina = disciplina;
        this.professor = professor;
        this.turno = turno;
        this.codigo = codigo;
    }

    public List<Matricula> matriculasAtivas() {
        return matriculas.stream().filter(Matricula::ativa).toList();
    }

    public boolean temVaga() {
        return matriculasAtivas().size() < disciplina.maxAlunos();
    }

    void registrar(Matricula matricula) {
        matriculas.add(matricula);
    }

    void remover(Matricula matricula) {
        matriculas.remove(matricula);
    }

    /** Cancela a turma se faltar quórum e devolve as matrículas que caíram junto. */
    List<Matricula> encerrarInscricoes() {
        var ativas = matriculasAtivas();
        if (ativas.size() < disciplina.minAlunos()) {
            estado = EstadoTurma.CANCELADA;
            ativas.forEach(Matricula::cancelarPorFaltaDeQuorum);
            return ativas;
        }
        estado = EstadoTurma.CONFIRMADA;
        return List.of();
    }

    public Long id() {
        return id;
    }

    public Curriculo curriculo() {
        return curriculo;
    }

    public Disciplina disciplina() {
        return disciplina;
    }

    public Professor professor() {
        return professor;
    }

    public Turno turno() {
        return turno;
    }

    public String codigo() {
        return codigo;
    }

    public EstadoTurma estado() {
        return estado;
    }
}
