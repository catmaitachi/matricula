package br.edu.matricula.api.web;

import br.edu.matricula.api.seguranca.UsuarioLogado;
import br.edu.matricula.api.servico.Comandos.NovaMatricula;
import br.edu.matricula.api.servico.MatriculaServico;
import br.edu.matricula.api.servico.Visoes.CurriculoParaAluno;
import br.edu.matricula.api.servico.Visoes.MatriculaVisao;
import br.edu.matricula.api.servico.Visoes.MinhasMatriculas;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** O aluno só age em nome de si mesmo: o id vem da sessão, nunca da requisição. */
@RestController
@RequestMapping("/api/aluno")
class AlunoControlador {

    private final MatriculaServico matriculas;

    AlunoControlador(MatriculaServico matriculas) {
        this.matriculas = matriculas;
    }

    @GetMapping("/curriculo")
    CurriculoParaAluno curriculo(@AuthenticationPrincipal UsuarioLogado aluno) {
        return matriculas.curriculo(aluno.id());
    }

    @GetMapping("/matriculas")
    MinhasMatriculas minhas(@AuthenticationPrincipal UsuarioLogado aluno) {
        return matriculas.minhas(aluno.id());
    }

    @PostMapping("/matriculas")
    @ResponseStatus(HttpStatus.CREATED)
    MatriculaVisao matricular(@AuthenticationPrincipal UsuarioLogado aluno, @Valid @RequestBody NovaMatricula pedido) {
        return matriculas.matricular(aluno.id(), pedido);
    }

    @DeleteMapping("/matriculas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancelar(@AuthenticationPrincipal UsuarioLogado aluno, @PathVariable Long id) {
        matriculas.cancelar(aluno.id(), id);
    }
}
