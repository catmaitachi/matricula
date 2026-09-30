package br.edu.matricula.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class ContasTest extends TesteBase {

    private MockHttpSession secretaria;

    @BeforeEach
    void entrarComoSecretaria() throws Exception {
        secretaria();
        secretaria = entrar("SEC001");
    }

    private static String aluno(String numPessoa, String senha, String numMatricula) {
        return """
                {"papel":"ALUNO","numPessoa":"%s","nome":"Ana Souza","senha":"%s","numMatricula":%s}"""
                .formatted(numPessoa, senha, numMatricula == null ? "null" : "\"" + numMatricula + "\"");
    }

    @Test
    void criaAlunoGuardandoSoOHashDaSenha() throws Exception {
        pedir(post("/api/contas").content(aluno("ALU5", "senha-forte-1", "20265")), secretaria)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numPessoa").value("ALU5"))
                .andExpect(jsonPath("$.numMatricula").value("20265"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        var guardado = usuarios.findByNumPessoa("ALU5").orElseThrow();
        assertThat(guardado.senhaHash()).startsWith("{bcrypt}").doesNotContain("senha-forte-1");
        assertThat(codificador.matches("senha-forte-1", guardado.senhaHash())).isTrue();
    }

    @Test
    void oNovoAlunoConsegueEntrar() throws Exception {
        pedir(post("/api/contas").content(aluno("ALU5", TesteBase.SENHA, "20265")), secretaria).andExpect(status().isCreated());
        entrar("ALU5");
    }

    @Test
    void recusaNumeroDePessoaRepetido() throws Exception {
        pedir(post("/api/contas").content(aluno("ALU5", "senha-forte-1", "1")), secretaria).andExpect(status().isCreated());
        pedir(post("/api/contas").content(aluno("ALU5", "senha-forte-1", "2")), secretaria)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("NUM_PESSOA_EM_USO"));
    }

    @Test
    void recusaNumeroDeMatriculaRepetido() throws Exception {
        pedir(post("/api/contas").content(aluno("ALU5", "senha-forte-1", "1")), secretaria).andExpect(status().isCreated());
        pedir(post("/api/contas").content(aluno("ALU6", "senha-forte-1", "1")), secretaria)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("NUM_MATRICULA_EM_USO"));
    }

    @Test
    void validaOsCampos() throws Exception {
        pedir(post("/api/contas").content(aluno("ALU5", "curta", "1")), secretaria)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.senha").exists());
        pedir(post("/api/contas").content(aluno("a b<script>", "senha-forte-1", "1")), secretaria)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.numPessoa").exists());
        pedir(post("/api/contas").content(aluno("ALU5", "senha-forte-1", null)), secretaria)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("DADO_INVALIDO"));
    }

    @Test
    void recusaSenhaQueOBcryptTruncaria() throws Exception {
        // 50 caracteres com acento = 100 bytes: passa no limite de caracteres, mas o bcrypt só olha 72 bytes
        pedir(post("/api/contas").content(aluno("ALU5", "é".repeat(50), "1")), secretaria)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("SENHA_INVALIDA"));
    }

    @Test
    void naoErraNaFormaComoOMalEscritoAJson() throws Exception {
        pedir(post("/api/contas").content("{isto não é json"), secretaria)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DADO_INVALIDO"));
    }

    @Test
    void listaSoOPapelPedido() throws Exception {
        aluno("ALU1");
        aluno("ALU2");
        professor("PROF1");

        mvc.perform(get("/api/contas?papel=ALUNO").session(secretaria)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/contas?papel=PROFESSOR").session(secretaria)).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/contas?papel=SECRETARIA").session(secretaria)).andExpect(status().isUnprocessableContent());
        mvc.perform(get("/api/contas").session(secretaria)).andExpect(status().isBadRequest());
    }

    @Test
    void atualizaDadosDesativaETrocaASenha() throws Exception {
        var aluno = aluno("ALU1");

        pedir(put("/api/contas/" + aluno.id()).content("""
                {"nome":"Novo Nome","ativo":false,"numMatricula":"999","novaSenha":"outra-senha-1"}"""), secretaria)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"))
                .andExpect(jsonPath("$.ativo").value(false));

        var guardado = usuarios.findByNumPessoa("ALU1").orElseThrow();
        assertThat(codificador.matches("outra-senha-1", guardado.senhaHash())).isTrue();
        mvc.perform(post("/api/auth/entrar").with(csrf()).param("numPessoa", "ALU1").param("senha", "outra-senha-1"))
                .andExpect(status().isUnauthorized()); // desativada
    }

    @Test
    void naoEditaContaDaSecretariaNemInexistente() throws Exception {
        var idSecretaria = usuarios.findByNumPessoa("SEC001").orElseThrow().id();
        var corpo = """
                {"nome":"X","ativo":true,"numMatricula":"1"}""";
        pedir(put("/api/contas/" + idSecretaria).content(corpo), secretaria).andExpect(status().isNotFound());
        pedir(put("/api/contas/999999").content(corpo), secretaria).andExpect(status().isNotFound());
    }
}
