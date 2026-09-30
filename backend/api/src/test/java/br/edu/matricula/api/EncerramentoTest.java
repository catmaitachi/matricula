package br.edu.matricula.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.matricula.api.servico.MatriculaServico;
import br.edu.matricula.api.servico.ReconciliadorDeCobrancas;
import br.edu.matricula.dominio.Curriculo;
import br.edu.matricula.dominio.EstadoMatricula;
import br.edu.matricula.dominio.EstadoTurma;
import br.edu.matricula.dominio.Matricula;
import br.edu.matricula.dominio.StatusCobranca;
import br.edu.matricula.dominio.TipoMatricula;
import br.edu.matricula.dominio.Turno;
import br.edu.matricula.pagamento.PagamentoFake.Situacao;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

class EncerramentoTest extends TesteBase {

    @Autowired MatriculaServico servico;
    @Autowired ReconciliadorDeCobrancas reconciliador;

    private MockHttpSession secretaria;
    private Curriculo curriculo;

    /** Duas turmas: Redes com 3 alunos (tem quórum) e Cálculo com 2 (não tem). */
    @BeforeEach
    void cenario() throws Exception {
        secretaria();
        var professor = professor("PROF1");
        curriculo = curriculoAberto(professor, disciplina("Redes", 3, 60), disciplina("Cálculo", 3, 60));
        var redes = curriculo.turmas().get(0);
        var calculo = curriculo.turmas().get(1);
        for (int i = 1; i <= 3; i++) {
            var a = aluno("ALU" + i);
            servico.matricular(a.id(), new br.edu.matricula.api.servico.Comandos.NovaMatricula(redes.id(), TipoMatricula.OBRIGATORIA));
            if (i <= 2) {
                servico.matricular(a.id(), new br.edu.matricula.api.servico.Comandos.NovaMatricula(calculo.id(), TipoMatricula.OBRIGATORIA));
            }
        }
        secretaria = entrar("SEC001");
    }

    @Test
    void cancelaATurmaSemQuorumECancelaAsCobrancas() throws Exception {
        var emCalculo = tx(() -> matriculas.findAll().stream().filter(m -> m.turma().disciplina().nome().equals("Cálculo")).map(Matricula::id).toList());

        pedir(post("/api/curriculos/" + curriculo.id() + "/encerrar"), secretaria)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turmasCanceladas").value(1))
                .andExpect(jsonPath("$.matriculasCanceladas").value(2));

        var turmasDepois = turmas.comDetalhes(curriculo.id());
        assertThat(turmasDepois).extracting(t -> t.disciplina().nome() + ":" + t.estado())
                .containsExactly("Redes:" + EstadoTurma.CONFIRMADA, "Cálculo:" + EstadoTurma.CANCELADA);
        assertThat(emCalculo).hasSize(2);
        for (var id : emCalculo) {
            var atual = matriculas.findById(id).orElseThrow();
            assertThat(atual.estado()).isEqualTo(EstadoMatricula.CANCELADA_SEM_QUORUM);
            assertThat(atual.cobranca()).isEqualTo(StatusCobranca.CANCELADA);
            assertThat(pagamento.situacao(atual.chaveDeCobranca())).contains(Situacao.CANCELADA);
        }
        assertThat(matriculas.findAll().stream().filter(Matricula::ativa)).hasSize(3); // as de Redes seguem valendo
    }

    @Test
    void depoisDeEncerradoNaoSeMatriculaNemSeCancela() throws Exception {
        pedir(post("/api/curriculos/" + curriculo.id() + "/encerrar"), secretaria).andExpect(status().isOk());
        var joao = entrar("ALU1");
        var idMatriculaDeRedes = tx(() -> matriculas.findAll().stream()
                .filter(m -> m.turma().disciplina().nome().equals("Redes") && m.aluno().numPessoa().equals("ALU1")).findFirst().orElseThrow().id());
        aluno("ALU9");
        var novo = entrar("ALU9");

        pedir(delete("/api/aluno/matriculas/" + idMatriculaDeRedes), joao)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("INSCRICOES_ENCERRADAS"));
        pedir(post("/api/aluno/matriculas").content("{\"turmaId\":%d,\"tipo\":\"OBRIGATORIA\"}".formatted(curriculo.turmas().get(0).id())), novo)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("INSCRICOES_ENCERRADAS"));
        mvc.perform(get("/api/aluno/curriculo").session(joao)).andExpect(status().isNotFound()); // não há mais currículo aberto
    }

    @Test
    void oAlunoVeQueATurmaCaiuPorFaltaDeQuorum() throws Exception {
        pedir(post("/api/curriculos/" + curriculo.id() + "/encerrar"), secretaria).andExpect(status().isOk());
        var joao = entrar("ALU1");

        mvc.perform(get("/api/aluno/matriculas").session(joao))
                .andExpect(jsonPath("$.estado").value("ENCERRADO"))
                .andExpect(jsonPath("$.obrigatorias").value(1)) // só a de Redes conta
                .andExpect(jsonPath("$.matriculas.length()").value(2))
                .andExpect(jsonPath("$.matriculas[?(@.disciplina=='Cálculo')].estado").value("CANCELADA_SEM_QUORUM"));
    }

    @Test
    void seOSistemaExternoFalharOCancelamentoFicaPendenteEOReconciliadorConclui() throws Exception {
        pagamento.indisponivel(true);
        pedir(post("/api/curriculos/" + curriculo.id() + "/encerrar"), secretaria).andExpect(status().isOk());

        var pendentes = matriculas.findByCobranca(StatusCobranca.CANCELAMENTO_PENDENTE);
        assertThat(pendentes).hasSize(2);

        pagamento.indisponivel(false);
        reconciliador.reconciliar(Duration.ZERO);

        assertThat(matriculas.findByCobranca(StatusCobranca.CANCELAMENTO_PENDENTE)).isEmpty();
        pendentes.forEach(m -> assertThat(pagamento.situacao(m.chaveDeCobranca())).contains(Situacao.CANCELADA));
    }

    @Test
    void naoEncerraDuasVezes() throws Exception {
        pedir(post("/api/curriculos/" + curriculo.id() + "/encerrar"), secretaria).andExpect(status().isOk());
        pedir(post("/api/curriculos/" + curriculo.id() + "/encerrar"), secretaria)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CURRICULO_NAO_ABERTO"));
    }

    @Test
    void oReconciliadorReenviaCobrancaQueFicouPelaMetade() throws Exception {
        matriculas.deleteAll();
        curriculos.deleteAll();
        var turma = turmaAberta(disciplina("Banco de Dados", 1, 60), professores.findAll().get(0));
        var alunoId = alunos.findAll().get(0).id();
        var pendente = tx(() -> matriculas.saveAndFlush(alunos.findById(alunoId).orElseThrow().matricular(turmas.findById(turma.id()).orElseThrow(), TipoMatricula.OBRIGATORIA)));
        assertThat(pendente.cobranca()).isEqualTo(StatusCobranca.PENDENTE);

        reconciliador.reconciliar(Duration.ofMinutes(2)); // recente demais: não mexe
        assertThat(matriculas.findById(pendente.id()).orElseThrow().cobranca()).isEqualTo(StatusCobranca.PENDENTE);

        reconciliador.reconciliar(Duration.ZERO);
        assertThat(matriculas.findById(pendente.id()).orElseThrow().cobranca()).isEqualTo(StatusCobranca.ENVIADA);
        assertThat(pagamento.situacao(pendente.chaveDeCobranca())).contains(Situacao.COBRADA);
    }
}
