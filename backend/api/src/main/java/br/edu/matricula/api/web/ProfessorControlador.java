package br.edu.matricula.api.web;

import br.edu.matricula.api.seguranca.UsuarioLogado;
import br.edu.matricula.api.servico.ProfessorServico;
import br.edu.matricula.api.servico.Visoes.AlunoNaTurma;
import br.edu.matricula.api.servico.Visoes.TurmaDoProfessor;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/professor")
class ProfessorControlador {

    private final ProfessorServico professor;

    ProfessorControlador(ProfessorServico professor) {
        this.professor = professor;
    }

    @GetMapping("/turmas")
    List<TurmaDoProfessor> turmas(@AuthenticationPrincipal UsuarioLogado logado) {
        return professor.turmas(logado.id());
    }

    @GetMapping("/turmas/{id}/alunos")
    List<AlunoNaTurma> alunos(@AuthenticationPrincipal UsuarioLogado logado, @PathVariable Long id) {
        return professor.alunos(logado.id(), id);
    }
}
