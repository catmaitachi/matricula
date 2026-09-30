package br.edu.matricula.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class SegurancaTest extends TesteBase {

    @Test
    void semSessaoRecebe401EmJson() throws Exception {
        mvc.perform(get("/api/aluno/curriculo"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
    }

    @Test
    void entrarSemTokenCsrfEhRecusado() throws Exception {
        aluno("ALU1");
        mvc.perform(post("/api/auth/entrar").param("numPessoa", "ALU1").param("senha", SENHA))
                .andExpect(status().isForbidden());
    }

    @Test
    void entraEIdentificaQuemEsta() throws Exception {
        aluno("ALU1");
        var sessao = entrar("ALU1");

        mvc.perform(get("/api/auth/eu").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numPessoa").value("ALU1"))
                .andExpect(jsonPath("$.papel").value("ALUNO"))
                .andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void senhaErradaEContaInexistenteTemAMesmaResposta() throws Exception {
        aluno("ALU1");
        var errada = mvc.perform(post("/api/auth/entrar").with(csrf()).param("numPessoa", "ALU1").param("senha", "x"))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        var inexistente = mvc.perform(post("/api/auth/entrar").with(csrf()).param("numPessoa", "NAOEXISTE").param("senha", "x"))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();

        assertThat(errada).isEqualTo(inexistente).contains("CREDENCIAIS_INVALIDAS");
    }

    @Test
    void contaDesativadaNaoEntra() throws Exception {
        var aluno = aluno("ALU1");
        aluno.ativo(false);
        alunos.save(aluno);

        mvc.perform(post("/api/auth/entrar").with(csrf()).param("numPessoa", "ALU1").param("senha", SENHA))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void bloqueiaDepoisDeCincoErrosMesmoComASenhaCerta() throws Exception {
        aluno("BLOQ1");
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/entrar").with(csrf()).param("numPessoa", "BLOQ1").param("senha", "errada" + i))
                    .andExpect(status().isUnauthorized());
        }

        mvc.perform(post("/api/auth/entrar").with(csrf()).param("numPessoa", "BLOQ1").param("senha", SENHA))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.codigo").value("MUITAS_TENTATIVAS"));
    }

    @Test
    void oBloqueioTambemVaiParaContasQueNaoExistem() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/entrar").with(csrf()).param("numPessoa", "FANTASMA").param("senha", "x"));
        }
        mvc.perform(post("/api/auth/entrar").with(csrf()).param("numPessoa", "FANTASMA").param("senha", "x"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void senhaEnormeNaoDerrubaOServidor() throws Exception {
        aluno("ALU1");
        mvc.perform(post("/api/auth/entrar").with(csrf()).param("numPessoa", "ALU1").param("senha", "a".repeat(20_000)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cadaPapelSoAcessaAsSuasRotas() throws Exception {
        aluno("ALU1");
        professor("PROF1");
        secretaria();
        var aluno = entrar("ALU1");
        var professor = entrar("PROF1");
        var secretaria = entrar("SEC001");

        mvc.perform(get("/api/contas?papel=ALUNO").session(aluno)).andExpect(status().isForbidden());
        mvc.perform(get("/api/curriculos").session(professor)).andExpect(status().isForbidden());
        mvc.perform(get("/api/aluno/matriculas").session(professor)).andExpect(status().isForbidden());
        mvc.perform(get("/api/professor/turmas").session(aluno)).andExpect(status().isForbidden());
        mvc.perform(get("/api/aluno/matriculas").session(secretaria)).andExpect(status().isForbidden());
        mvc.perform(get("/api/contas?papel=ALUNO").session(secretaria)).andExpect(status().isOk());
    }

    @Test
    void quemFoiDesativadoPerdeAAcessoNaProximaRequisicao() throws Exception {
        var aluno = aluno("ALU1");
        var sessao = entrar("ALU1");
        mvc.perform(get("/api/auth/eu").session(sessao)).andExpect(status().isOk());

        aluno.ativo(false);
        alunos.save(aluno);

        mvc.perform(get("/api/auth/eu").session(sessao)).andExpect(status().isUnauthorized());
    }

    @Test
    void sairEncerraASessao() throws Exception {
        aluno("ALU1");
        var sessao = entrar("ALU1");

        mvc.perform(post("/api/auth/sair").session(sessao).with(csrf())).andExpect(status().isNoContent());

        mvc.perform(get("/api/auth/eu").session(sessao)).andExpect(status().isUnauthorized());
    }

    @Test
    void respostasTemCabecalhosDeSeguranca() throws Exception {
        mvc.perform(get("/api/aluno/curriculo"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")))
                .andExpect(header().string("Cache-Control", containsString("no-store")));
    }

    @Test
    void saudeEPublicaMasOrestoDoActuatorNao() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
        mvc.perform(get("/h2-console")).andExpect(status().isUnauthorized());
    }
}
