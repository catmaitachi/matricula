package br.edu.matricula.api;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.matricula.api.servico.Comandos.NovaMatricula;
import br.edu.matricula.api.servico.MatriculaServico;
import br.edu.matricula.dominio.Curriculo;
import br.edu.matricula.dominio.RegraDeNegocioException;
import br.edu.matricula.dominio.TipoMatricula;
import br.edu.matricula.dominio.Turno;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** RNF03: sob disputa, o sistema não vende a mesma vaga duas vezes nem fura o limite de um aluno. */
class ConcorrenciaTest extends TesteBase {

    @Autowired MatriculaServico servico;

    /** Dispara as tarefas ao mesmo tempo e conta quantas terminaram sem erro e quantas com cada código de regra. */
    private static List<String> disputar(List<Runnable> tarefas) throws Exception {
        var pool = Executors.newFixedThreadPool(tarefas.size());
        var largada = new CountDownLatch(1);
        var resultados = new ArrayList<Future<String>>();
        for (var tarefa : tarefas) {
            resultados.add(pool.submit(() -> {
                largada.await();
                try {
                    tarefa.run();
                    return "OK";
                } catch (RegraDeNegocioException e) {
                    return e.codigo();
                }
            }));
        }
        largada.countDown();
        var saidas = new ArrayList<String>();
        for (var r : resultados) {
            saidas.add(r.get());
        }
        pool.shutdown();
        return saidas;
    }

    @Test
    void muitosAlunosDisputandoAUltimaVagaSoUmLeva() throws Exception {
        var turma = turmaAberta(disciplina("Seminário", 1, 1), professor("PROF1"));
        var tarefas = new ArrayList<Runnable>();
        for (int i = 1; i <= 8; i++) {
            var alunoId = aluno("ALU" + i).id();
            tarefas.add(() -> servico.matricular(alunoId, new NovaMatricula(turma.id(), TipoMatricula.OPTATIVA)));
        }

        var saidas = disputar(tarefas);

        assertThat(saidas).filteredOn("OK"::equals).hasSize(1);
        assertThat(saidas).filteredOn("TURMA_LOTADA"::equals).hasSize(7);
        assertThat(matriculas.count()).isEqualTo(1);
    }

    @Test
    void omesmoAlunoClicandoVariasVezesNaoFuraOLimite() throws Exception {
        var curriculo = new Curriculo("2026/2");
        var professor = professor("PROF1");
        for (int i = 1; i <= 6; i++) {
            curriculo.adicionarTurma(disciplina("D" + i, 1, 60), professor, Turno.MANHA);
        }
        curriculo.abrir();
        var turmasDoSemestre = curriculos.saveAndFlush(curriculo).turmas();
        var alunoId = aluno("ALU1").id();
        var sucessos = new AtomicInteger();
        var tarefas = new ArrayList<Runnable>();
        for (var turma : turmasDoSemestre) {
            tarefas.add(() -> servico.matricular(alunoId, new NovaMatricula(turma.id(), TipoMatricula.OBRIGATORIA)));
        }

        var saidas = disputar(tarefas);

        sucessos.set((int) saidas.stream().filter("OK"::equals).count());
        assertThat(sucessos.get()).isEqualTo(4); // só 4 obrigatórias, mesmo com 6 pedidos simultâneos
        assertThat(saidas).filteredOn("LIMITE_OBRIGATORIAS"::equals).hasSize(2);
        assertThat(matriculas.count()).isEqualTo(4);
    }

    @Test
    void oMesmoPedidoRepetidoAoMesmoTempoGeraUmaMatricula() throws Exception {
        var turma = turmaAberta(disciplina("Redes", 1, 60), professor("PROF1"));
        var alunoId = aluno("ALU1").id();
        var tarefas = new ArrayList<Runnable>();
        for (int i = 0; i < 5; i++) {
            tarefas.add(() -> servico.matricular(alunoId, new NovaMatricula(turma.id(), TipoMatricula.OBRIGATORIA)));
        }

        var saidas = disputar(tarefas);

        assertThat(saidas).filteredOn("OK"::equals).hasSize(1);
        assertThat(saidas).filteredOn("JA_MATRICULADO"::equals).hasSize(4);
        assertThat(matriculas.count()).isEqualTo(1);
    }
}
