package br.edu.matricula.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("ALUNO")
public class Aluno extends Usuario {

    @Column(unique = true, length = 20)
    private String numMatricula;

    @OneToMany(mappedBy = "aluno")
    private final List<Matricula> matriculas = new ArrayList<>();

    protected Aluno() {
    }

    public Aluno(String numPessoa, String nome, String senhaHash, String numMatricula) {
        super(numPessoa, nome, senhaHash);
        this.numMatricula = Validacao.texto(numMatricula, 20, "Nº de matrícula");
    }

    @Override
    public Papel papel() {
        return Papel.ALUNO;
    }

    private boolean mesmo(Aluno outro) {
        return this == outro || (id() != null && id().equals(outro.id()));
    }

    public String numMatricula() {
        return numMatricula;
    }

    public void alterarNumMatricula(String numMatricula) {
        this.numMatricula = Validacao.texto(numMatricula, 20, "Nº de matrícula");
    }

    public List<Matricula> matriculasAtivasEm(Curriculo curriculo) {
        return matriculas.stream()
                .filter(m -> m.ativa() && m.turma().curriculo().mesmo(curriculo))
                .toList();
    }

    /**
     * Matricula o aluno na turma. Regras, nesta ordem: inscrições abertas, disciplina ainda
     * não cursada no semestre, vaga na turma e limite por tipo (4 obrigatórias, 2 optativas).
     */
    public Matricula matricular(Turma turma, TipoMatricula tipo) {
        var curriculo = turma.curriculo();
        if (curriculo.estado() != EstadoCurriculo.ABERTO) {
            throw new RegraDeNegocioException("INSCRICOES_ENCERRADAS", "As inscrições deste semestre não estão abertas.");
        }
        var doSemestre = matriculasAtivasEm(curriculo);
        if (doSemestre.stream().anyMatch(m -> m.turma().disciplina().mesma(turma.disciplina()))) {
            throw new RegraDeNegocioException("JA_MATRICULADO",
                    "Você já está matriculado em " + turma.disciplina().nome() + " neste semestre.");
        }
        if (!turma.temVaga()) {
            throw new RegraDeNegocioException("TURMA_LOTADA", "A turma está lotada.");
        }
        var doTipo = doSemestre.stream().filter(m -> m.tipo() == tipo).count();
        if (doTipo >= tipo.limite()) {
            throw new RegraDeNegocioException(
                    tipo == TipoMatricula.OBRIGATORIA ? "LIMITE_OBRIGATORIAS" : "LIMITE_OPTATIVAS",
                    "Limite de " + tipo.limite() + " disciplinas " + tipo.plural() + " atingido. Cancele uma para continuar.");
        }
        var matricula = new Matricula(this, turma, tipo);
        matriculas.add(matricula);
        turma.registrar(matricula);
        return matricula;
    }

    /** Desfaz a matrícula. Só é possível enquanto o semestre está aberto. */
    public void cancelar(Matricula matricula) {
        if (!mesmo(matricula.aluno())) {
            throw new RegraDeNegocioException("MATRICULA_ALHEIA", "Esta matrícula não é sua.");
        }
        if (matricula.turma().curriculo().estado() != EstadoCurriculo.ABERTO) {
            throw new RegraDeNegocioException("INSCRICOES_ENCERRADAS",
                    "Depois de encerradas as inscrições não é possível cancelar a matrícula.");
        }
        matriculas.remove(matricula);
        matricula.turma().remover(matricula);
    }
}
