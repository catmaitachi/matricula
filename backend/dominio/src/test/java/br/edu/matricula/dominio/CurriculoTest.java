package br.edu.matricula.dominio;

import static br.edu.matricula.dominio.Cenario.aluno;
import static br.edu.matricula.dominio.Cenario.disciplina;
import static br.edu.matricula.dominio.Cenario.professor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;

class CurriculoTest {

    private static void espera(Runnable acao, String codigo) {
        assertThatThrownBy(acao::run)
                .isInstanceOf(RegraDeNegocioException.class)
                .extracting(e -> ((RegraDeNegocioException) e).codigo())
                .isEqualTo(codigo);
    }

    @Test
    void aceitaSomenteSemestreNoFormatoAnoBarraUmOuDois() {
        new Curriculo("2026/1");
        new Curriculo("2026/2");
        for (var invalido : new String[] {"2026", "2026/3", "26/2", "2026-2", "", " "}) {
            espera(() -> new Curriculo(invalido), "SEMESTRE_INVALIDO");
        }
        espera(() -> new Curriculo(null), "SEMESTRE_INVALIDO");
    }

    @Test
    void numeraAsTurmasDeUmaMesmaDisciplinaEmOrdem() {
        var curriculo = new Curriculo("2026/2");
        var redes = disciplina("Redes", 3, 60);
        var a = curriculo.adicionarTurma(redes, professor(), Turno.MANHA);
        var b = curriculo.adicionarTurma(redes, professor(), Turno.NOITE);
        var outra = curriculo.adicionarTurma(disciplina("Cálculo", 3, 60), professor(), Turno.TARDE);

        assertThat(a.codigo()).isEqualTo("A");
        assertThat(b.codigo()).isEqualTo("B");
        assertThat(outra.codigo()).isEqualTo("A");
    }

    @Test
    void naoAbreSemTurmasNemEditaDepoisDeAberto() {
        var curriculo = new Curriculo("2026/2");
        espera(curriculo::abrir, "CURRICULO_VAZIO");

        var turma = curriculo.adicionarTurma(disciplina("Redes", 3, 60), professor(), Turno.MANHA);
        curriculo.abrir();

        espera(() -> curriculo.adicionarTurma(disciplina("Cálculo", 3, 60), professor(), Turno.MANHA), "CURRICULO_FECHADO_PARA_EDICAO");
        espera(() -> curriculo.removerTurma(turma), "CURRICULO_FECHADO_PARA_EDICAO");
        espera(curriculo::abrir, "CURRICULO_FECHADO_PARA_EDICAO");
    }

    @Test
    void naoOfereceDisciplinaNemProfessorInativos() {
        var curriculo = new Curriculo("2026/2");
        var inativa = disciplina("Antiga", 3, 60);
        inativa.ativa(false);
        espera(() -> curriculo.adicionarTurma(inativa, professor(), Turno.MANHA), "DISCIPLINA_INATIVA");

        var afastado = professor();
        afastado.ativo(false);
        espera(() -> curriculo.adicionarTurma(disciplina("Redes", 3, 60), afastado, Turno.MANHA), "PROFESSOR_INATIVO");
    }

    @Test
    void encerrarConfirmaTurmaComQuorumECancelaAQueNaoTem() {
        var curriculo = new Curriculo("2026/2");
        var comQuorum = curriculo.adicionarTurma(disciplina("Redes", 3, 60), professor(), Turno.MANHA);
        var semQuorum = curriculo.adicionarTurma(disciplina("Cálculo", 3, 60), professor(), Turno.MANHA);
        curriculo.abrir();
        for (int i = 1; i <= 3; i++) { // exatamente o mínimo: tem quórum
            aluno(i).matricular(comQuorum, TipoMatricula.OBRIGATORIA);
        }
        for (int i = 1; i <= 2; i++) { // um a menos que o mínimo: cai
            aluno(i).matricular(semQuorum, TipoMatricula.OBRIGATORIA);
        }

        var resultado = curriculo.encerrar();

        assertThat(curriculo.estado()).isEqualTo(EstadoCurriculo.ENCERRADO);
        assertThat(comQuorum.estado()).isEqualTo(EstadoTurma.CONFIRMADA);
        assertThat(semQuorum.estado()).isEqualTo(EstadoTurma.CANCELADA);
        assertThat(resultado.turmasCanceladas()).containsExactly(semQuorum);
        assertThat(resultado.matriculasCanceladas()).hasSize(2)
                .allSatisfy(m -> assertThat(m.estado()).isEqualTo(EstadoMatricula.CANCELADA_SEM_QUORUM));
        assertThat(comQuorum.matriculasAtivas()).hasSize(3);
    }

    @Test
    void turmaVaziaTambemEhCancelada() {
        var curriculo = new Curriculo("2026/2");
        var vazia = curriculo.adicionarTurma(disciplina("Redes", 3, 60), professor(), Turno.MANHA);
        curriculo.abrir();

        var resultado = curriculo.encerrar();

        assertThat(vazia.estado()).isEqualTo(EstadoTurma.CANCELADA);
        assertThat(resultado.matriculasCanceladas()).isEmpty();
    }

    @Test
    void cobrancaJaEnviadaFicaComCancelamentoPendenteEANaoEnviadaSeCancelaDireto() {
        var curriculo = new Curriculo("2026/2");
        var turma = curriculo.adicionarTurma(disciplina("Redes", 3, 60), professor(), Turno.MANHA);
        curriculo.abrir();
        var enviada = aluno(1).matricular(turma, TipoMatricula.OBRIGATORIA);
        enviada.cobrancaEnviada();
        var pendente = aluno(2).matricular(turma, TipoMatricula.OBRIGATORIA);

        curriculo.encerrar();

        assertThat(enviada.cobranca()).isEqualTo(StatusCobranca.CANCELAMENTO_PENDENTE);
        assertThat(pendente.cobranca()).isEqualTo(StatusCobranca.CANCELADA);
    }

    @Test
    void soEncerraCurriculoAberto() {
        var curriculo = new Curriculo("2026/2");
        espera(curriculo::encerrar, "CURRICULO_NAO_ABERTO");
    }

    @Test
    void disciplinaRejeitaLimitesIncoerentes() {
        espera(() -> new Disciplina("X", 0, 10, Set.of()), "LIMITES_INVALIDOS");
        espera(() -> new Disciplina("X", 5, 4, Set.of()), "LIMITES_INVALIDOS");
        new Disciplina("X", 5, 5, Set.of());
    }
}
