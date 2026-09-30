package br.edu.matricula.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/** Toda falha de quem chama vira {codigo, mensagem} com o status certo, sem vazar detalhe interno. */
class ErrosTest extends TesteBase {

    @Test
    void rotaInexistenteParaQuemEstaLogadoEh404EmJson() throws Exception {
        aluno("ALU1");
        var sessao = entrar("ALU1");

        mvc.perform(get("/api/nao-existe").session(sessao))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
        mvc.perform(get("/h2-console").session(sessao)).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/env").session(sessao)).andExpect(status().isNotFound());
    }

    @Test
    void metodoENaoAceitosViramErroDeQuemChamou() throws Exception {
        aluno("ALU1");
        var sessao = entrar("ALU1");

        pedir(patch("/api/aluno/matriculas"), sessao)
                .andExpect(status().isMethodNotAllowed()).andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"));
        mvc.perform(post("/api/aluno/matriculas").session(sessao)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType("text/plain").content("x"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void erroNuncaVazaClasseNemPilhaNemSql() throws Exception {
        aluno("ALU1");
        var sessao = entrar("ALU1");

        var corpo = pedir(post("/api/aluno/matriculas").content("{\"turmaId\":\"'; DROP TABLE usuarios;--\",\"tipo\":\"OBRIGATORIA\"}"), sessao)
                .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(corpo).doesNotContain("Exception", "at br.edu", "org.hibernate", "SELECT", "DROP");
    }
}
