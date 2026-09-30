package br.edu.matricula.api.seguranca;

import br.edu.matricula.api.repositorio.UsuarioRepositorio;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

@Configuration
class ConfiguracaoSeguranca {

    private static void json(HttpServletResponse resposta, int status, String codigo, String mensagem) throws IOException {
        resposta.setStatus(status);
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // só textos fixos: nada do que o usuário digitou entra na resposta
        resposta.getWriter().write("{\"codigo\":\"" + codigo + "\",\"mensagem\":\"" + mensagem + "\"}");
    }

    @Bean
    SecurityFilterChain filtros(HttpSecurity http, UsuarioRepositorio usuarios, TentativasDeLogin tentativas) throws Exception {
        http
                // sessão em cookie HttpOnly + token CSRF em cookie legível, devolvido pelo app no cabeçalho X-XSRF-TOKEN
                .csrf(CsrfConfigurer::spa)
                .cors(cors -> cors.disable()) // o app é servido na mesma origem (proxy em desenvolvimento)
                .addFilterAfter(new FiltroDeUsuarioAtivo(usuarios), SecurityContextHolderFilter.class)
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/entrar").permitAll()
                        .requestMatchers("/api/contas/**", "/api/cursos/**", "/api/disciplinas/**", "/api/curriculos/**")
                        .hasRole("SECRETARIA")
                        .requestMatchers("/api/aluno/**").hasRole("ALUNO")
                        .requestMatchers("/api/professor/**").hasRole("PROFESSOR")
                        .anyRequest().authenticated())
                .formLogin(f -> f
                        .loginProcessingUrl("/api/auth/entrar")
                        .usernameParameter("numPessoa")
                        .passwordParameter("senha")
                        .successHandler((req, res, auth) -> {
                            tentativas.sucesso(req.getParameter("numPessoa"));
                            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        })
                        .failureHandler((req, res, erro) -> {
                            if (erro instanceof LockedException) {
                                json(res, 429, "MUITAS_TENTATIVAS", "Muitas tentativas de entrada. Tente de novo em alguns minutos.");
                                return;
                            }
                            if (erro instanceof BadCredentialsException || erro instanceof DisabledException
                                    || erro instanceof org.springframework.security.core.userdetails.UsernameNotFoundException) {
                                tentativas.falhou(req.getParameter("numPessoa"));
                            }
                            json(res, 401, "CREDENCIAIS_INVALIDAS", "Nº de pessoa ou senha incorretos.");
                        }))
                .logout(l -> l
                        .logoutUrl("/api/auth/sair")
                        .deleteCookies("MATRICULA_SESSAO")
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT)))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, erro) ->
                                json(res, 401, "NAO_AUTENTICADO", "Sua sessão expirou. Entre de novo."))
                        .accessDeniedHandler((req, res, erro) ->
                                json(res, 403, "ACESSO_NEGADO", "Você não tem permissão para isso.")))
                // API: não há "voltar para a página pedida"; sem isto, cada 401 anônimo criaria uma sessão no servidor
                .requestCache(cache -> cache.disable())
                .sessionManagement(s -> s.sessionFixation(f -> f.changeSessionId()))
                .headers(h -> h
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                        .referrerPolicy(r -> r.policy(ReferrerPolicy.NO_REFERRER))
                        .frameOptions(f -> f.deny()))
                .httpBasic(b -> b.disable());
        return http.build();
    }
}
