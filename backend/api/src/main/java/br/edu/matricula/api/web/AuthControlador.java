package br.edu.matricula.api.web;

import br.edu.matricula.api.repositorio.UsuarioRepositorio;
import br.edu.matricula.api.seguranca.UsuarioLogado;
import br.edu.matricula.api.servico.NaoEncontradoException;
import br.edu.matricula.api.servico.Visoes.UsuarioVisao;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Entrar e sair são tratados pela cadeia de segurança (POST /api/auth/entrar e /api/auth/sair). */
@RestController
@RequestMapping("/api/auth")
class AuthControlador {

    private final UsuarioRepositorio usuarios;

    AuthControlador(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    /** Quem sou eu. Lê do banco, então nome e situação estão sempre atuais. */
    @GetMapping("/eu")
    UsuarioVisao eu(@AuthenticationPrincipal UsuarioLogado logado) {
        return usuarios.findById(logado.id()).map(UsuarioVisao::de)
                .orElseThrow(() -> new NaoEncontradoException("Conta não encontrada."));
    }
}
