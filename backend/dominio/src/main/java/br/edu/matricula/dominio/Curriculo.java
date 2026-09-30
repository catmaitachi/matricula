package br.edu.matricula.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Oferta de um semestre: as turmas que a secretaria montou. Os alunos só veem o currículo ABERTO. */
@Entity
public class Curriculo {

    private static final Pattern SEMESTRE = Pattern.compile("\\d{4}/[12]");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 6)
    private String semestre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoCurriculo estado = EstadoCurriculo.RASCUNHO;

    @OneToMany(mappedBy = "curriculo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private final List<Turma> turmas = new ArrayList<>();

    protected Curriculo() {
    }

    public Curriculo(String semestre) {
        if (semestre == null || !SEMESTRE.matcher(semestre.strip()).matches()) {
            throw new RegraDeNegocioException("SEMESTRE_INVALIDO", "Informe o semestre no formato AAAA/1 ou AAAA/2, por exemplo 2026/2.");
        }
        this.semestre = semestre.strip();
    }

    public Turma adicionarTurma(Disciplina disciplina, Professor professor, Turno turno) {
        exigirRascunho();
        if (!disciplina.ativa()) {
            throw new RegraDeNegocioException("DISCIPLINA_INATIVA", "A disciplina " + disciplina.nome() + " está inativa.");
        }
        if (!professor.ativo()) {
            throw new RegraDeNegocioException("PROFESSOR_INATIVO", "O professor " + professor.nome() + " está inativo.");
        }
        var mesmas = turmas.stream().filter(t -> t.disciplina().mesma(disciplina)).count();
        if (mesmas >= 26) {
            throw new RegraDeNegocioException("TURMAS_DEMAIS", "Uma disciplina comporta no máximo 26 turmas por semestre.");
        }
        var turma = new Turma(this, disciplina, professor, turno, String.valueOf((char) ('A' + mesmas)));
        turmas.add(turma);
        return turma;
    }

    public void removerTurma(Turma turma) {
        exigirRascunho();
        turmas.remove(turma);
    }

    public void abrir() {
        exigirRascunho();
        if (turmas.isEmpty()) {
            throw new RegraDeNegocioException("CURRICULO_VAZIO", "Adicione pelo menos uma turma antes de abrir as inscrições.");
        }
        estado = EstadoCurriculo.ABERTO;
    }

    /** Fecha as inscrições: turma com menos alunos que o mínimo é cancelada, junto com suas matrículas. */
    public ResultadoEncerramento encerrar() {
        if (estado != EstadoCurriculo.ABERTO) {
            throw new RegraDeNegocioException("CURRICULO_NAO_ABERTO", "Só é possível encerrar um currículo com inscrições abertas.");
        }
        estado = EstadoCurriculo.ENCERRADO;
        var turmasCanceladas = new ArrayList<Turma>();
        var matriculasCanceladas = new ArrayList<Matricula>();
        for (var turma : turmas) {
            var canceladas = turma.encerrarInscricoes();
            if (turma.estado() == EstadoTurma.CANCELADA) {
                turmasCanceladas.add(turma);
            }
            matriculasCanceladas.addAll(canceladas);
        }
        return new ResultadoEncerramento(turmasCanceladas, matriculasCanceladas);
    }

    public boolean mesmo(Curriculo outro) {
        return this == outro || (id() != null && id().equals(outro.id()));
    }

    private void exigirRascunho() {
        if (estado != EstadoCurriculo.RASCUNHO) {
            throw new RegraDeNegocioException("CURRICULO_FECHADO_PARA_EDICAO", "Só é possível editar um currículo em rascunho.");
        }
    }

    public Long id() {
        return id;
    }

    public String semestre() {
        return semestre;
    }

    public EstadoCurriculo estado() {
        return estado;
    }

    public List<Turma> turmas() {
        return List.copyOf(turmas);
    }
}
