package br.edu.matricula.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.matricula.api.servico.Comandos.NovaMatricula;
import br.edu.matricula.api.servico.MatriculaServico;
import br.edu.matricula.dominio.TipoMatricula;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProfessorTest extends TesteBase {

    @Autowired MatriculaServico servico;

    @Test
    void veSoAsPropriasTurmasEOsAlunosMatriculados() throws Exception {
        var carlos = professor("PROF1");
        var helena = professor("PROF2");
        var curriculo = curriculoAberto(carlos, disciplina("Redes", 3, 60));
        var turmaDeCarlos = curriculo.turmas().get(0);
        var joao = aluno("ALU1");
        var maria = aluno("ALU2");
        servico.matricular(joao.id(), new NovaMatricula(turmaDeCarlos.id(), TipoMatricula.OBRIGATORIA));
        servico.matricular(maria.id(), new NovaMatricula(turmaDeCarlos.id(), TipoMatricula.OPTATIVA));
        var sessaoCarlos = entrar("PROF1");
        var sessaoHelena = entrar("PROF2");

        mvc.perform(get("/api/professor/turmas").session(sessaoCarlos))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].disciplina").value("Redes"))
                .andExpect(jsonPath("$[0].matriculados").value(2));
        mvc.perform(get("/api/professor/turmas").session(sessaoHelena)).andExpect(jsonPath("$.length()").value(0));

        mvc.perform(get("/api/professor/turmas/" + turmaDeCarlos.id() + "/alunos").session(sessaoCarlos))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Aluno ALU1"))
                .andExpect(jsonPath("$[0].numMatricula").value("MALU1"))
                .andExpect(jsonPath("$[0].tipo").value("OBRIGATORIA"))
                .andExpect(jsonPath("$[0].senhaHash").doesNotExist());
    }

    @Test
    void turmaDeOutroProfessorEhComoSeNaoExistisse() throws Exception {
        var carlos = professor("PROF1");
        professor("PROF2");
        var turma = curriculoAberto(carlos, disciplina("Redes", 3, 60)).turmas().get(0);
        var sessaoHelena = entrar("PROF2");

        mvc.perform(get("/api/professor/turmas/" + turma.id() + "/alunos").session(sessaoHelena)).andExpect(status().isNotFound());
        mvc.perform(get("/api/professor/turmas/424242/alunos").session(sessaoHelena)).andExpect(status().isNotFound());
    }

    @Test
    void naoVeTurmasDeCurriculoEmRascunho() throws Exception {
        var carlos = professor("PROF1");
        var rascunho = new br.edu.matricula.dominio.Curriculo("2027/1");
        rascunho.adicionarTurma(disciplina("Redes", 3, 60), carlos, br.edu.matricula.dominio.Turno.MANHA);
        curriculos.saveAndFlush(rascunho);
        var sessao = entrar("PROF1");

        mvc.perform(get("/api/professor/turmas").session(sessao)).andExpect(jsonPath("$.length()").value(0));
    }
}
