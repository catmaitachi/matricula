package br.edu.matricula.api.servico;

import br.edu.matricula.dominio.Aluno;
import br.edu.matricula.dominio.Curriculo;
import br.edu.matricula.dominio.Curso;
import br.edu.matricula.dominio.Disciplina;
import br.edu.matricula.dominio.EstadoCurriculo;
import br.edu.matricula.dominio.EstadoMatricula;
import br.edu.matricula.dominio.EstadoTurma;
import br.edu.matricula.dominio.Matricula;
import br.edu.matricula.dominio.Papel;
import br.edu.matricula.dominio.StatusCobranca;
import br.edu.matricula.dominio.TipoMatricula;
import br.edu.matricula.dominio.Turma;
import br.edu.matricula.dominio.Turno;
import br.edu.matricula.dominio.Usuario;
import java.util.Comparator;
import java.util.List;

/** Saídas da API. Nenhuma expõe hash de senha, ids internos de outros usuários ou entidades. */
public final class Visoes {

    private Visoes() {
    }

    public record UsuarioVisao(String nome, String numPessoa, Papel papel) {
        public static UsuarioVisao de(Usuario u) {
            return new UsuarioVisao(u.nome(), u.numPessoa(), u.papel());
        }
    }

    public record ContaVisao(Long id, String numPessoa, String nome, Papel papel, String numMatricula, boolean ativo) {
        public static ContaVisao de(Usuario u) {
            return new ContaVisao(u.id(), u.numPessoa(), u.nome(), u.papel(),
                    u instanceof Aluno a ? a.numMatricula() : null, u.ativo());
        }
    }

    public record CursoVisao(Long id, String nome, int numCreditos) {
        public static CursoVisao de(Curso c) {
            return new CursoVisao(c.id(), c.nome(), c.numCreditos());
        }
    }

    public record DisciplinaVisao(Long id, String nome, int minAlunos, int maxAlunos, boolean ativa, List<CursoVisao> cursos) {
        public static DisciplinaVisao de(Disciplina d) {
            return new DisciplinaVisao(d.id(), d.nome(), d.minAlunos(), d.maxAlunos(), d.ativa(),
                    d.cursos().stream().map(CursoVisao::de).sorted(Comparator.comparing(CursoVisao::nome)).toList());
        }
    }

    public record TurmaVisao(Long id, Long disciplinaId, String disciplina, String codigo, Turno turno, Long professorId,
                             String professor, EstadoTurma estado, int ocupadas, int minAlunos, int maxAlunos) {
        public static TurmaVisao de(Turma t) {
            return new TurmaVisao(t.id(), t.disciplina().id(), t.disciplina().nome(), t.codigo(), t.turno(),
                    t.professor().id(), t.professor().nome(), t.estado(), t.matriculasAtivas().size(),
                    t.disciplina().minAlunos(), t.disciplina().maxAlunos());
        }
    }

    public record CurriculoResumo(Long id, String semestre, EstadoCurriculo estado, int turmas) {
        public static CurriculoResumo de(Curriculo c) {
            return new CurriculoResumo(c.id(), c.semestre(), c.estado(), c.turmas().size());
        }
    }

    public record CurriculoVisao(Long id, String semestre, EstadoCurriculo estado, List<TurmaVisao> turmas) {
    }

    public record EncerramentoVisao(int turmasCanceladas, int matriculasCanceladas) {
    }

    /** Turma como o aluno a vê; {@code minhaMatriculaId} é preenchido se ele já está nela. */
    public record TurmaParaAluno(Long id, String disciplina, String codigo, Turno turno, String professor,
                                 EstadoTurma estado, int ocupadas, int minAlunos, int maxAlunos, Long minhaMatriculaId) {
    }

    public record CurriculoParaAluno(String semestre, EstadoCurriculo estado, List<TurmaParaAluno> turmas) {
    }

    public record MatriculaVisao(Long id, Long turmaId, String disciplina, String codigo, Turno turno, TipoMatricula tipo,
                                 EstadoMatricula estado, StatusCobranca cobranca) {
        public static MatriculaVisao de(Matricula m) {
            var t = m.turma();
            return new MatriculaVisao(m.id(), t.id(), t.disciplina().nome(), t.codigo(), t.turno(), m.tipo(), m.estado(), m.cobranca());
        }
    }

    public record MinhasMatriculas(String semestre, EstadoCurriculo estado, int obrigatorias, int limiteObrigatorias,
                                   int optativas, int limiteOptativas, List<MatriculaVisao> matriculas) {
    }

    public record TurmaDoProfessor(Long id, String semestre, String disciplina, String codigo, Turno turno,
                                   EstadoTurma estado, int matriculados, int minAlunos, int maxAlunos) {
    }

    public record AlunoNaTurma(String nome, String numPessoa, String numMatricula, TipoMatricula tipo) {
    }
}
