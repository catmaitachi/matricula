package br.edu.matricula.api;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.matricula.api.repositorio.UsuarioRepositorio;
import br.edu.matricula.dominio.Aluno;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Contra um servidor de verdade (porta aleatória), sem os atalhos do MockMvc: é o único jeito de conferir os
 * atributos reais dos cookies e o fluxo de CSRF que o navegador vai executar.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SegurancaHttpTest {

    @LocalServerPort int porta;
    @Autowired UsuarioRepositorio usuarios;
    @Autowired PasswordEncoder codificador;

    private final HttpClient http = HttpClient.newHttpClient(); // sem gerenciador de cookies: nós controlamos cada um

    @BeforeEach
    void criarAluno() {
        usuarios.findByNumPessoa("HTTP1").ifPresent(usuarios::delete);
        usuarios.save(new Aluno("HTTP1", "Aluno Http", codificador.encode("senha-de-teste-1"), "MHTTP1"));
    }

    private HttpResponse<String> enviar(String metodo, String caminho, String corpoForm, String... cabecalhos) throws Exception {
        var pedido = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho))
                .method(metodo, corpoForm == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(corpoForm));
        if (corpoForm != null) {
            pedido.header("Content-Type", "application/x-www-form-urlencoded");
        }
        if (cabecalhos.length > 0) {
            pedido.headers(cabecalhos);
        }
        return http.send(pedido.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String cookie(HttpResponse<?> resposta, String nome) {
        return resposta.headers().allValues("set-cookie").stream().filter(c -> c.startsWith(nome + "=")).findFirst().orElse(null);
    }

    private static String valor(String setCookie) {
        return setCookie.substring(setCookie.indexOf('=') + 1, setCookie.indexOf(';'));
    }

    private static String form(String numPessoa, String senha) {
        return "numPessoa=" + URLEncoder.encode(numPessoa, StandardCharsets.UTF_8) + "&senha=" + URLEncoder.encode(senha, StandardCharsets.UTF_8);
    }

    @Test
    void oPrimeiroAcessoEntregaOCookieCsrfLegivelPeloApp() throws Exception {
        var resposta = enviar("GET", "/api/auth/eu", null);

        assertThat(resposta.statusCode()).isEqualTo(401);
        var xsrf = cookie(resposta, "XSRF-TOKEN");
        assertThat(xsrf).isNotNull();
        assertThat(xsrf.toLowerCase()).doesNotContain("httponly"); // o JavaScript precisa lê-lo
        assertThat(cookie(resposta, "MATRICULA_SESSAO")).isNull(); // quem não entrou não ocupa sessão no servidor
    }

    @Test
    void oCookieDeSessaoNaoEhLegivelPorScriptEsoViajaNoMesmoSite() throws Exception {
        var xsrf = valor(cookie(enviar("GET", "/api/auth/eu", null), "XSRF-TOKEN"));

        var login = enviar("POST", "/api/auth/entrar", form("HTTP1", "senha-de-teste-1"),
                "Cookie", "XSRF-TOKEN=" + xsrf, "X-XSRF-TOKEN", xsrf);

        assertThat(login.statusCode()).isEqualTo(204);
        var sessao = cookie(login, "MATRICULA_SESSAO").toLowerCase();
        assertThat(sessao).contains("httponly").contains("secure").contains("samesite=strict");
    }

    @Test
    void semOCabecalhoCsrfOLoginEhRecusadoMesmoComOCookie() throws Exception {
        var xsrf = valor(cookie(enviar("GET", "/api/auth/eu", null), "XSRF-TOKEN"));

        var login = enviar("POST", "/api/auth/entrar", form("HTTP1", "senha-de-teste-1"), "Cookie", "XSRF-TOKEN=" + xsrf);

        assertThat(login.statusCode()).isEqualTo(403);
        assertThat(cookie(login, "MATRICULA_SESSAO")).isNull();
    }

    @Test
    void sairTambemExigeCsrf() throws Exception {
        var xsrf = valor(cookie(enviar("GET", "/api/auth/eu", null), "XSRF-TOKEN"));
        var login = enviar("POST", "/api/auth/entrar", form("HTTP1", "senha-de-teste-1"), "Cookie", "XSRF-TOKEN=" + xsrf, "X-XSRF-TOKEN", xsrf);
        var sessao = valor(cookie(login, "MATRICULA_SESSAO"));
        var cookies = "XSRF-TOKEN=" + xsrf + "; MATRICULA_SESSAO=" + sessao;

        assertThat(enviar("GET", "/api/auth/eu", null, "Cookie", cookies).statusCode()).isEqualTo(200);
        assertThat(enviar("POST", "/api/auth/sair", "", "Cookie", cookies).statusCode()).isEqualTo(403);
        assertThat(enviar("POST", "/api/auth/sair", "", "Cookie", cookies, "X-XSRF-TOKEN", xsrf).statusCode()).isEqualTo(204);
        assertThat(enviar("GET", "/api/auth/eu", null, "Cookie", cookies).statusCode()).isEqualTo(401);
    }

    @Test
    void oIdDaSessaoMudaDepoisDoLoginContraFixacaoDeSessao() throws Exception {
        var xsrf = valor(cookie(enviar("GET", "/api/auth/eu", null), "XSRF-TOKEN"));
        var primeiro = valor(cookie(enviar("POST", "/api/auth/entrar", form("HTTP1", "senha-de-teste-1"),
                "Cookie", "XSRF-TOKEN=" + xsrf, "X-XSRF-TOKEN", xsrf), "MATRICULA_SESSAO"));
        var segundo = valor(cookie(enviar("POST", "/api/auth/entrar", form("HTTP1", "senha-de-teste-1"),
                "Cookie", "XSRF-TOKEN=" + xsrf, "X-XSRF-TOKEN", xsrf), "MATRICULA_SESSAO"));

        assertThat(List.of(primeiro, segundo)).doesNotHaveDuplicates();
    }
}
