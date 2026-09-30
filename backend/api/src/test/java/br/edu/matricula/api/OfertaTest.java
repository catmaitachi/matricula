package br.edu.matricula.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class OfertaTest extends TesteBase {

    private MockHttpSession secretaria;

    @BeforeEach
    void entrarComoSecretaria() throws Exception {
        secretaria();
        secretaria = entrar("SEC001");
    }

    private long criarCurso(String nome) throws Exception {
        return idDe(pedir(post("/api/cursos").content("{\"nome\":\"%s\",\"numCreditos\":240}".formatted(nome)), secretaria)
                .andExpect(status().isCreated()));
    }

    private static String disciplina(String nome, int min, int max, String cursoIds) {
        return "{\"nome\":\"%s\",\"minAlunos\":%d,\"maxAlunos\":%d,\"ativa\":true,\"cursoIds\":[%s]}".formatted(nome, min, max, cursoIds);
    }

    @Test
    void criaCursosERecusaNomeRepetido() throws Exception {
        criarCurso("Engenharia de Software");
        pedir(post("/api/cursos").content("{\"nome\":\"Engenharia de Software\",\"numCreditos\":10}"), secretaria)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("NOME_EM_USO"));
        pedir(post("/api/cursos").content("{\"nome\":\"Outro\",\"numCreditos\":0}"), secretaria)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.numCreditos").exists());
    }

    @Test
    void criaDisciplinaLigadaAosCursos() throws Exception {
        var curso = criarCurso("Engenharia de Software");
        pedir(post("/api/disciplinas").content(disciplina("Redes", 3, 60, String.valueOf(curso))), secretaria)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.minAlunos").value(3))
                .andExpect(jsonPath("$.cursos[0].nome").value("Engenharia de Software"));
        pedir(post("/api/disciplinas").content(disciplina("Redes", 3, 60, "")), secretaria).andExpect(status().isConflict());
    }

    @Test
    void recusaLimitesIncoerentesECursoInexistente() throws Exception {
        pedir(post("/api/disciplinas").content(disciplina("Redes", 10, 5, "")), secretaria)
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.codigo").value("LIMITES_INVALIDOS"));
        pedir(post("/api/disciplinas").content(disciplina("Redes", 3, 60, "424242")), secretaria).andExpect(status().isNotFound());
    }

    @Test
    void montaOCurriculoAbreEnaoDeixaEditarDepois() throws Exception {
        var redes = disciplina("Redes", 3, 60);
        var professor = professor("PROF1");

        pedir(post("/api/curriculos").content("{\"semestre\":\"2026-2\"}"), secretaria)
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.codigo").value("SEMESTRE_INVALIDO"));
        var id = idDe(pedir(post("/api/curriculos").content("{\"semestre\":\"2026/2\"}"), secretaria)
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("RASCUNHO")));
        pedir(post("/api/curriculos").content("{\"semestre\":\"2026/2\"}"), secretaria).andExpect(status().isConflict());

        pedir(post("/api/curriculos/" + id + "/abrir"), secretaria)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CURRICULO_VAZIO"));

        var turma = "{\"disciplinaId\":%d,\"professorId\":%d,\"turno\":\"NOITE\"}".formatted(redes.id(), professor.id());
        pedir(post("/api/curriculos/" + id + "/turmas").content(turma), secretaria)
                .andExpect(status().isCreated()).andExpect(jsonPath("$.codigo").value("A")).andExpect(jsonPath("$.disciplina").value("Redes"));
        pedir(post("/api/curriculos/" + id + "/turmas").content(turma), secretaria).andExpect(jsonPath("$.codigo").value("B"));

        pedir(post("/api/curriculos/" + id + "/abrir"), secretaria).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ABERTO"));
        pedir(post("/api/curriculos/" + id + "/turmas").content(turma), secretaria)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CURRICULO_FECHADO_PARA_EDICAO"));
        mvc.perform(get("/api/curriculos/" + id).session(secretaria)).andExpect(jsonPath("$.turmas.length()").value(2));
    }

    @Test
    void soUmCurriculoPodeEstarAbertoPorVez() throws Exception {
        var professor = professor("PROF1");
        curriculoAberto(professor, disciplina("Redes", 3, 60));
        var id = idDe(pedir(post("/api/curriculos").content("{\"semestre\":\"2027/1\"}"), secretaria));
        var disciplina = disciplina("Cálculo", 3, 60);
        pedir(post("/api/curriculos/" + id + "/turmas").content("{\"disciplinaId\":%d,\"professorId\":%d,\"turno\":\"MANHA\"}"
                .formatted(disciplina.id(), professor.id())), secretaria).andExpect(status().isCreated());

        pedir(post("/api/curriculos/" + id + "/abrir"), secretaria)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("JA_HA_CURRICULO_ABERTO"));
    }

    @Test
    void naoMudaOsLimitesDeUmaDisciplinaComInscricoesAbertas() throws Exception {
        var redes = disciplina("Redes", 3, 60);
        curriculoAberto(professor("PROF1"), redes);

        pedir(put("/api/disciplinas/" + redes.id()).content(disciplina("Redes", 3, 30, "")), secretaria)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("DISCIPLINA_EM_USO"));
        pedir(put("/api/disciplinas/" + redes.id()).content(disciplina("Redes de Computadores", 3, 60, "")), secretaria)
                .andExpect(status().isOk()); // renomear pode
    }

    @Test
    void removeTurmaSoEmRascunho() throws Exception {
        var professor = professor("PROF1");
        var redes = disciplina("Redes", 3, 60);
        var id = idDe(pedir(post("/api/curriculos").content("{\"semestre\":\"2026/1\"}"), secretaria).andExpect(status().isCreated()));
        var turmaId = idDe(pedir(post("/api/curriculos/" + id + "/turmas").content("{\"disciplinaId\":%d,\"professorId\":%d,\"turno\":\"TARDE\"}"
                .formatted(redes.id(), professor.id())), secretaria).andExpect(status().isCreated()));

        pedir(delete("/api/curriculos/" + id + "/turmas/" + turmaId), secretaria).andExpect(status().isNoContent());
        mvc.perform(get("/api/curriculos/" + id).session(secretaria)).andExpect(jsonPath("$.turmas.length()").value(0));
        pedir(delete("/api/curriculos/" + id + "/turmas/" + turmaId), secretaria).andExpect(status().isNotFound());
    }

    @Test
    void recursoInexistenteEhNaoEncontrado() throws Exception {
        mvc.perform(get("/api/curriculos/424242").session(secretaria)).andExpect(status().isNotFound());
        pedir(post("/api/curriculos/424242/abrir"), secretaria).andExpect(status().isNotFound());
    }
}
