package br.edu.matricula.api.servico;

import br.edu.matricula.api.repositorio.MatriculaRepositorio;
import br.edu.matricula.api.repositorio.TurmaRepositorio;
import br.edu.matricula.api.servico.Visoes.AlunoNaTurma;
import br.edu.matricula.api.servico.Visoes.TurmaDoProfessor;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF03: o professor vê os alunos das suas turmas, e só delas. */
@Service
@Transactional(readOnly = true)
public class ProfessorServico {

    private final TurmaRepositorio turmas;
    private final MatriculaRepositorio matriculas;

    ProfessorServico(TurmaRepositorio turmas, MatriculaRepositorio matriculas) {
        this.turmas = turmas;
        this.matriculas = matriculas;
    }

    public List<TurmaDoProfessor> turmas(Long professorId) {
        return turmas.doProfessor(professorId).stream()
                .map(t -> new TurmaDoProfessor(t.id(), t.curriculo().semestre(), t.disciplina().nome(), t.codigo(), t.turno(),
                        t.estado(), t.matriculasAtivas().size(), t.disciplina().minAlunos(), t.disciplina().maxAlunos()))
                .toList();
    }

    public List<AlunoNaTurma> alunos(Long professorId, Long turmaId) {
        // turma de outro professor responde igual a turma inexistente
        var turma = turmas.findById(turmaId)
                .filter(t -> t.professor().id().equals(professorId))
                .orElseThrow(() -> new NaoEncontradoException("Turma não encontrada."));
        return matriculas.ativasDaTurma(turma.id()).stream()
                .map(m -> new AlunoNaTurma(m.aluno().nome(), m.aluno().numPessoa(), m.aluno().numMatricula(), m.tipo()))
                .toList();
    }
}
