package br.edu.matricula.dominio;

import static br.edu.matricula.dominio.Cenario.aluno;
import static br.edu.matricula.dominio.Cenario.disciplina;
import static br.edu.matricula.dominio.Cenario.professor;
import static br.edu.matricula.dominio.Cenario.turmaAberta;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AlunoMatriculaTest {

    private static void espera(Runnable acao, String codigo) {
        assertThatThrownBy(acao::run)
                .isInstanceOf(RegraDeNegocioException.class)
                .extracting(e -> ((RegraDeNegocioException) e).codigo())
                .isEqualTo(codigo);
    }

    /** Currículo aberto com n disciplinas, uma turma A de cada. */
    private static Curriculo curriculoCom(int disciplinas) {
        var curriculo = new Curriculo("2026/2");
        for (int i = 1; i <= disciplinas; i++) {
            curriculo.adicionarTurma(disciplina("Disciplina " + i, 3, 60), professor(), Turno.MANHA);
        }
        curriculo.abrir();
        return curriculo;
    }

    @Test
    void matriculaOAlunoNaTurma() {
        var turma = turmaAberta(disciplina("Engenharia de Software", 3, 60));
        var aluno = aluno(1);

        var matricula = aluno.matricular(turma, TipoMatricula.OBRIGATORIA);

        assertThat(matricula.ativa()).isTrue();
        assertThat(matricula.cobranca()).isEqualTo(StatusCobranca.PENDENTE);
        assertThat(turma.matriculasAtivas()).containsExactly(matricula);
        assertThat(aluno.matriculasAtivasEm(turma.curriculo())).containsExactly(matricula);
    }

    @Test
    void naoMatriculaEmCurriculoQueNaoEstaAberto() {
        var rascunho = new Curriculo("2026/2");
        var turma = rascunho.adicionarTurma(disciplina("Cálculo", 3, 60), professor(), Turno.TARDE);
        espera(() -> aluno(1).matricular(turma, TipoMatricula.OBRIGATORIA), "INSCRICOES_ENCERRADAS");

        rascunho.abrir();
        rascunho.encerrar();
        espera(() -> aluno(1).matricular(turma, TipoMatricula.OBRIGATORIA), "INSCRICOES_ENCERRADAS");
    }

    @Test
    void naoMatriculaDuasVezesNaMesmaDisciplinaNoSemestre() {
        var curriculo = new Curriculo("2026/2");
        var calculo = disciplina("Cálculo II", 3, 60);
        var turmaA = curriculo.adicionarTurma(calculo, professor(), Turno.MANHA);
        var turmaB = curriculo.adicionarTurma(calculo, professor(), Turno.NOITE);
        curriculo.abrir();
        var aluno = aluno(1);
        aluno.matricular(turmaA, TipoMatricula.OBRIGATORIA);

        espera(() -> aluno.matricular(turmaB, TipoMatricula.OBRIGATORIA), "JA_MATRICULADO");
        espera(() -> aluno.matricular(turmaA, TipoMatricula.OBRIGATORIA), "JA_MATRICULADO");
    }

    @Test
    void respeitaOLimiteDeVagasDaDisciplina() {
        var turma = turmaAberta(disciplina("Redes", 1, 60));
        for (int i = 1; i <= 59; i++) {
            aluno(i).matricular(turma, TipoMatricula.OBRIGATORIA);
        }
        assertThat(turma.temVaga()).isTrue();

        aluno(60).matricular(turma, TipoMatricula.OBRIGATORIA); // a 60ª ocupa a última vaga

        assertThat(turma.temVaga()).isFalse();
        espera(() -> aluno(61).matricular(turma, TipoMatricula.OBRIGATORIA), "TURMA_LOTADA");
    }

    @Test
    void limitaAQuatroObrigatorias() {
        var curriculo = curriculoCom(6);
        var aluno = aluno(1);
        for (int i = 0; i < 4; i++) {
            aluno.matricular(curriculo.turmas().get(i), TipoMatricula.OBRIGATORIA);
        }

        espera(() -> aluno.matricular(curriculo.turmas().get(4), TipoMatricula.OBRIGATORIA), "LIMITE_OBRIGATORIAS");
    }

    @Test
    void limitaADuasOptativas() {
        var curriculo = curriculoCom(3);
        var aluno = aluno(1);
        aluno.matricular(curriculo.turmas().get(0), TipoMatricula.OPTATIVA);
        aluno.matricular(curriculo.turmas().get(1), TipoMatricula.OPTATIVA);

        espera(() -> aluno.matricular(curriculo.turmas().get(2), TipoMatricula.OPTATIVA), "LIMITE_OPTATIVAS");
    }

    @Test
    void aceitaSeisNoTotalQuandoSaoQuatroMaisDuas() {
        var curriculo = curriculoCom(7);
        var aluno = aluno(1);
        for (int i = 0; i < 4; i++) {
            aluno.matricular(curriculo.turmas().get(i), TipoMatricula.OBRIGATORIA);
        }
        aluno.matricular(curriculo.turmas().get(4), TipoMatricula.OPTATIVA);
        aluno.matricular(curriculo.turmas().get(5), TipoMatricula.OPTATIVA);

        assertThat(aluno.matriculasAtivasEm(curriculo)).hasSize(6);
        espera(() -> aluno.matricular(curriculo.turmas().get(6), TipoMatricula.OBRIGATORIA), "LIMITE_OBRIGATORIAS");
        espera(() -> aluno.matricular(curriculo.turmas().get(6), TipoMatricula.OPTATIVA), "LIMITE_OPTATIVAS");
    }

    @Test
    void cancelarLiberaAVagaEOLimite() {
        var turma = turmaAberta(disciplina("Redes", 1, 1));
        var primeiro = aluno(1);
        var matricula = primeiro.matricular(turma, TipoMatricula.OBRIGATORIA);
        espera(() -> aluno(2).matricular(turma, TipoMatricula.OBRIGATORIA), "TURMA_LOTADA");

        primeiro.cancelar(matricula);

        assertThat(turma.temVaga()).isTrue();
        assertThat(primeiro.matriculasAtivasEm(turma.curriculo())).isEmpty();
        aluno(2).matricular(turma, TipoMatricula.OBRIGATORIA);
    }

    @Test
    void naoCancelaDepoisDeEncerradas() {
        var turma = turmaAberta(disciplina("Redes", 1, 60));
        var aluno = aluno(1);
        var matricula = aluno.matricular(turma, TipoMatricula.OBRIGATORIA);
        turma.curriculo().encerrar();

        espera(() -> aluno.cancelar(matricula), "INSCRICOES_ENCERRADAS");
    }

    @Test
    void naoCancelaMatriculaDeOutroAluno() {
        var turma = turmaAberta(disciplina("Redes", 1, 60));
        var matricula = aluno(1).matricular(turma, TipoMatricula.OBRIGATORIA);

        espera(() -> aluno(2).cancelar(matricula), "MATRICULA_ALHEIA");
    }
}
