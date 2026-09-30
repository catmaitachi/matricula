package br.edu.matricula.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.matricula.dominio.EstadoMatricula;
import br.edu.matricula.dominio.Matricula;
import br.edu.matricula.dominio.StatusCobranca;
import br.edu.matricula.dominio.Turma;
import br.edu.matricula.pagamento.PagamentoFake.Situacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

class MatriculaTest extends TesteBase {

    private MockHttpSession joao;
    private Turma redes;

    @BeforeEach
    void cenario() throws Exception {
        aluno("ALU1");
        redes = turmaAberta(disciplina("Redes", 3, 60), professor("PROF1"));
        joao = entrar("ALU1");
    }

    private ResultActions matricular(MockHttpSession sessao, Long turmaId, String tipo) throws Exception {
        return pedir(post("/api/aluno/matriculas").content("{\"turmaId\":%d,\"tipo\":\"%s\"}".formatted(turmaId, tipo)), sessao);
    }

    @Test
    void semCurriculoAbertoNaoHaOQueMostrar() throws Exception {
        matriculas.deleteAll();
        curriculos.deleteAll();
        mvc.perform(get("/api/aluno/curriculo").session(joao))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("SEM_CURRICULO_ABERTO"));
    }

    @Test
    void mostraAsTurmasComVagasEAMinhaMatricula() throws Exception {
        mvc.perform(get("/api/aluno/curriculo").session(joao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semestre").value("2026/2"))
                .andExpect(jsonPath("$.turmas[0].disciplina").value("Redes"))
                .andExpect(jsonPath("$.turmas[0].ocupadas").value(0))
                .andExpect(jsonPath("$.turmas[0].maxAlunos").value(60))
                .andExpect(jsonPath("$.turmas[0].minhaMatriculaId").doesNotExist());

        var id = idDe(matricular(joao, redes.id(), "OBRIGATORIA"));

        mvc.perform(get("/api/aluno/curriculo").session(joao))
                .andExpect(jsonPath("$.turmas[0].ocupadas").value(1))
                .andExpect(jsonPath("$.turmas[0].minhaMatriculaId").value(id));
    }

    @Test
    void matriculaECobraNoSistemaExterno() throws Exception {
        matricular(joao, redes.id(), "OBRIGATORIA")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.disciplina").value("Redes"))
                .andExpect(jsonPath("$.tipo").value("OBRIGATORIA"))
                .andExpect(jsonPath("$.cobranca").value("ENVIADA"));

        var matricula = matriculas.findAll().get(0);
        assertThat(pagamento.situacao(matricula.chaveDeCobranca())).contains(Situacao.COBRADA);
    }

    @Test
    void naoMatriculaDuasVezesNaMesmaTurma() throws Exception {
        matricular(joao, redes.id(), "OBRIGATORIA").andExpect(status().isCreated());
        matricular(joao, redes.id(), "OPTATIVA")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("JA_MATRICULADO"));
        assertThat(matriculas.count()).isEqualTo(1);
    }

    @Test
    void respeitaOLimiteDeQuatroObrigatorias() throws Exception {
        var professor = professores.findAll().get(0);
        var cinco = new br.edu.matricula.dominio.Curriculo("2027/1");
        for (int i = 1; i <= 5; i++) {
            cinco.adicionarTurma(disciplina("Extra " + i, 3, 60), professor, br.edu.matricula.dominio.Turno.TARDE);
        }
        cinco.abrir();
        matriculas.deleteAll();
        curriculos.deleteAll();
        var turmasExtra = curriculos.saveAndFlush(cinco).turmas();
        for (int i = 0; i < 4; i++) {
            matricular(joao, turmasExtra.get(i).id(), "OBRIGATORIA").andExpect(status().isCreated());
        }

        matricular(joao, turmasExtra.get(4).id(), "OBRIGATORIA")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("LIMITE_OBRIGATORIAS"));
        matricular(joao, turmasExtra.get(4).id(), "OPTATIVA").andExpect(status().isCreated()); // optativa tem limite próprio

        mvc.perform(get("/api/aluno/matriculas").session(joao))
                .andExpect(jsonPath("$.obrigatorias").value(4)).andExpect(jsonPath("$.limiteObrigatorias").value(4))
                .andExpect(jsonPath("$.optativas").value(1)).andExpect(jsonPath("$.limiteOptativas").value(2));
    }

    @Test
    void turmaLotadaNaoAceitaMaisUm() throws Exception {
        matriculas.deleteAll();
        curriculos.deleteAll();
        var pequena = turmaAberta(disciplina("Seminário", 1, 1), professores.findAll().get(0));
        aluno("ALU2");
        var maria = entrar("ALU2");
        matricular(joao, pequena.id(), "OPTATIVA").andExpect(status().isCreated());

        matricular(maria, pequena.id(), "OPTATIVA")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("TURMA_LOTADA"));
    }

    @Test
    void cancelaELiberaACobranca() throws Exception {
        var id = idDe(matricular(joao, redes.id(), "OBRIGATORIA"));
        var chave = Matricula.chaveDeCobranca(id);

        pedir(delete("/api/aluno/matriculas/" + id), joao).andExpect(status().isNoContent());

        assertThat(matriculas.count()).isZero();
        assertThat(pagamento.situacao(chave)).contains(Situacao.CANCELADA);
        matricular(joao, redes.id(), "OBRIGATORIA").andExpect(status().isCreated()); // pode voltar a se matricular
    }

    @Test
    void ninguemCancelaAMatriculaDeOutraPessoa() throws Exception {
        aluno("ALU2");
        var maria = entrar("ALU2");
        var id = idDe(matricular(joao, redes.id(), "OBRIGATORIA"));

        pedir(delete("/api/aluno/matriculas/" + id), maria).andExpect(status().isNotFound());

        assertThat(matriculas.count()).isEqualTo(1);
    }

    @Test
    void semOSistemaDePagamentoNaoHaMatricula() throws Exception {
        pagamento.indisponivel(true);

        matricular(joao, redes.id(), "OBRIGATORIA")
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.codigo").value("PAGAMENTO_INDISPONIVEL"));

        assertThat(matriculas.count()).isZero();
        pagamento.indisponivel(false);
        matricular(joao, redes.id(), "OBRIGATORIA").andExpect(status().isCreated()); // e a vaga não ficou presa
    }

    @Test
    void semOSistemaDePagamentoNaoSeCancela() throws Exception {
        var id = idDe(matricular(joao, redes.id(), "OBRIGATORIA"));
        pagamento.indisponivel(true);

        pedir(delete("/api/aluno/matriculas/" + id), joao).andExpect(status().isServiceUnavailable());

        assertThat(matriculas.count()).isEqualTo(1); // nada mudou
    }

    @Test
    void recusaPedidosInvalidos() throws Exception {
        matricular(joao, 424242L, "OBRIGATORIA").andExpect(status().isNotFound());
        pedir(post("/api/aluno/matriculas").content("{\"turmaId\":%d,\"tipo\":\"QUALQUER\"}".formatted(redes.id())), joao)
                .andExpect(status().isBadRequest());
        pedir(post("/api/aluno/matriculas").content("{\"tipo\":\"OBRIGATORIA\"}"), joao)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.turmaId").exists());
    }

    @Test
    void oIdDoAlunoSempreVemDaSessao() throws Exception {
        // não existe campo alunoId no pedido: mesmo que alguém o envie, ele é ignorado
        aluno("ALU2");
        var alheio = alunos.findAll().stream().filter(a -> a.numPessoa().equals("ALU2")).findFirst().orElseThrow();
        pedir(post("/api/aluno/matriculas").content("{\"turmaId\":%d,\"tipo\":\"OBRIGATORIA\",\"alunoId\":%d}".formatted(redes.id(), alheio.id())), joao)
                .andExpect(status().isCreated());

        assertThat(matriculas.findAll()).singleElement().satisfies(m -> {
            assertThat(m.estado()).isEqualTo(EstadoMatricula.ATIVA);
            assertThat(m.cobranca()).isEqualTo(StatusCobranca.ENVIADA);
        });
        assertThat(alunos.findAll().stream().filter(a -> a.numPessoa().equals("ALU1")).count()).isEqualTo(1);
    }
}
