package br.edu.matricula.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.matricula.api.repositorio.AlunoRepositorio;
import br.edu.matricula.api.repositorio.CurriculoRepositorio;
import br.edu.matricula.api.repositorio.CursoRepositorio;
import br.edu.matricula.api.repositorio.DisciplinaRepositorio;
import br.edu.matricula.api.repositorio.MatriculaRepositorio;
import br.edu.matricula.api.repositorio.ProfessorRepositorio;
import br.edu.matricula.api.repositorio.TurmaRepositorio;
import br.edu.matricula.api.repositorio.UsuarioRepositorio;
import br.edu.matricula.dominio.Aluno;
import br.edu.matricula.dominio.Curriculo;
import br.edu.matricula.dominio.Disciplina;
import br.edu.matricula.dominio.Professor;
import br.edu.matricula.dominio.Secretaria;
import br.edu.matricula.dominio.Turma;
import br.edu.matricula.dominio.Turno;
import br.edu.matricula.pagamento.PagamentoFake;
import com.jayway.jsonpath.JsonPath;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Sobe a aplicação inteira com banco em memória. Cada teste começa com o banco vazio. */
@SpringBootTest
@AutoConfigureMockMvc
abstract class TesteBase {

    static final String SENHA = "senha-de-teste-1";

    @Autowired protected MockMvc mvc;
    @Autowired protected PasswordEncoder codificador;
    @Autowired protected UsuarioRepositorio usuarios;
    @Autowired protected AlunoRepositorio alunos;
    @Autowired protected ProfessorRepositorio professores;
    @Autowired protected CursoRepositorio cursos;
    @Autowired protected DisciplinaRepositorio disciplinas;
    @Autowired protected CurriculoRepositorio curriculos;
    @Autowired protected TurmaRepositorio turmas;
    @Autowired protected MatriculaRepositorio matriculas;
    @Autowired protected PagamentoFake pagamento;
    @Autowired private PlatformTransactionManager gerenciador;

    /** Roda numa transação, para os testes poderem navegar relações preguiçosas. */
    protected <T> T tx(Supplier<T> acao) {
        return new TransactionTemplate(gerenciador).execute(s -> acao.get());
    }

    @BeforeEach
    void limparBanco() {
        matriculas.deleteAll();
        curriculos.deleteAll();
        disciplinas.deleteAll();
        cursos.deleteAll();
        usuarios.deleteAll();
        pagamento.indisponivel(false);
    }

    protected Secretaria secretaria() {
        return usuarios.save(new Secretaria("SEC001", "Maria Silva", codificador.encode(SENHA)));
    }

    protected Professor professor(String numPessoa) {
        return professores.save(new Professor(numPessoa, "Prof. " + numPessoa, codificador.encode(SENHA)));
    }

    protected Aluno aluno(String numPessoa) {
        return alunos.save(new Aluno(numPessoa, "Aluno " + numPessoa, codificador.encode(SENHA), "M" + numPessoa));
    }

    protected Disciplina disciplina(String nome, int min, int max) {
        return disciplinas.save(new Disciplina(nome, min, max, Set.of()));
    }

    /** Currículo 2026/2 aberto com uma turma A da disciplina, lecionada pelo professor. */
    protected Turma turmaAberta(Disciplina disciplina, Professor professor) {
        var curriculo = new Curriculo("2026/2");
        curriculo.adicionarTurma(disciplina, professor, Turno.NOITE);
        curriculo.abrir();
        return curriculos.saveAndFlush(curriculo).turmas().get(0);
    }

    /** Currículo 2026/2 aberto com uma turma A de cada disciplina. */
    protected Curriculo curriculoAberto(Professor professor, Disciplina... disciplinas) {
        var curriculo = new Curriculo("2026/2");
        for (var d : disciplinas) {
            curriculo.adicionarTurma(d, professor, Turno.MANHA);
        }
        curriculo.abrir();
        return curriculos.saveAndFlush(curriculo);
    }

    /** Entra de verdade (pelo filtro de login) e devolve a sessão. */
    protected MockHttpSession entrar(String numPessoa) throws Exception {
        var resultado = mvc.perform(post("/api/auth/entrar").with(csrf()).param("numPessoa", numPessoa).param("senha", SENHA))
                .andExpect(status().isNoContent())
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }

    /** O campo {@code id} de uma resposta JSON. */
    protected static long idDe(ResultActions resposta) throws Exception {
        return ((Number) JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
    }

    protected ResultActions pedir(MockHttpServletRequestBuilder requisicao, MockHttpSession sessao) throws Exception {
        return mvc.perform(requisicao.session(sessao).with(csrf()).contentType("application/json"));
    }
}
