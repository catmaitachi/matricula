package br.edu.matricula.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.matricula.dominio.Papel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"matricula.bootstrap.num-pessoa=SECBOOT", "matricula.bootstrap.senha=uma-senha-longa-123"})
class PrimeiraSecretariaTest extends TesteBase {

    @Autowired PrimeiraSecretaria primeira;

    @Test
    void criaAPrimeiraSecretariaQuandoAVariavelEstaDefinidaEElaConsegueEntrar() throws Exception {
        primeira.run(new DefaultApplicationArguments());

        var criada = usuarios.findByNumPessoa("SECBOOT").orElseThrow();
        assertThat(criada.papel()).isEqualTo(Papel.SECRETARIA);
        assertThat(criada.senhaHash()).startsWith("{bcrypt}").doesNotContain("uma-senha-longa-123");
        mvc.perform(post("/api/auth/entrar").with(csrf()).param("numPessoa", "SECBOOT").param("senha", "uma-senha-longa-123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void naoDuplicaNemTrocaASenhaDeUmaContaQueJaExiste() throws Exception {
        primeira.run(new DefaultApplicationArguments());
        var hashAntes = usuarios.findByNumPessoa("SECBOOT").orElseThrow().senhaHash();

        primeira.run(new DefaultApplicationArguments());

        assertThat(usuarios.count()).isEqualTo(1);
        assertThat(usuarios.findByNumPessoa("SECBOOT").orElseThrow().senhaHash()).isEqualTo(hashAntes);
    }

    @Test
    void recusaSenhaCurtaEmVezDeCriarUmaContaFraca() {
        var fraca = new PrimeiraSecretaria(usuarios, codificador, "SECFRACA", "X", "curta");
        assertThatThrownBy(() -> fraca.run(new DefaultApplicationArguments())).isInstanceOf(IllegalStateException.class);
        assertThat(usuarios.existsByNumPessoa("SECFRACA")).isFalse();
    }

    @Test
    void semAVariavelNaoFazNada() throws Exception {
        new PrimeiraSecretaria(usuarios, codificador, "SECNADA", "X", "").run(new DefaultApplicationArguments());
        assertThat(usuarios.count()).isZero();
    }
}
