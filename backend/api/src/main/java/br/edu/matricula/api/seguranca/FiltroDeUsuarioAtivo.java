package br.edu.matricula.api.seguranca;

import br.edu.matricula.api.repositorio.UsuarioRepositorio;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Derruba a sessão de quem foi desativado depois de entrar: a sessão sozinha não perceberia. */
final class FiltroDeUsuarioAtivo extends OncePerRequestFilter {

    private final UsuarioRepositorio usuarios;

    FiltroDeUsuarioAtivo(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null && autenticacao.getPrincipal() instanceof UsuarioLogado logado
                && !usuarios.existsByIdAndAtivoTrue(logado.id())) {
            SecurityContextHolder.clearContext();
            var sessao = requisicao.getSession(false);
            if (sessao != null) {
                sessao.invalidate();
            }
        }
        cadeia.doFilter(requisicao, resposta);
    }
}
