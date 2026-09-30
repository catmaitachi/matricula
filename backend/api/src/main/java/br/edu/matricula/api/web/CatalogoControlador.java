package br.edu.matricula.api.web;

import br.edu.matricula.api.servico.CatalogoServico;
import br.edu.matricula.api.servico.Comandos.DadosCurso;
import br.edu.matricula.api.servico.Comandos.DadosDisciplina;
import br.edu.matricula.api.servico.Visoes.CursoVisao;
import br.edu.matricula.api.servico.Visoes.DisciplinaVisao;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class CatalogoControlador {

    private final CatalogoServico catalogo;

    CatalogoControlador(CatalogoServico catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/cursos")
    List<CursoVisao> cursos() {
        return catalogo.cursos();
    }

    @PostMapping("/cursos")
    @ResponseStatus(HttpStatus.CREATED)
    CursoVisao criarCurso(@Valid @RequestBody DadosCurso curso) {
        return catalogo.criarCurso(curso);
    }

    @PutMapping("/cursos/{id}")
    CursoVisao atualizarCurso(@PathVariable Long id, @Valid @RequestBody DadosCurso curso) {
        return catalogo.atualizarCurso(id, curso);
    }

    @GetMapping("/disciplinas")
    List<DisciplinaVisao> disciplinas() {
        return catalogo.disciplinas();
    }

    @PostMapping("/disciplinas")
    @ResponseStatus(HttpStatus.CREATED)
    DisciplinaVisao criarDisciplina(@Valid @RequestBody DadosDisciplina disciplina) {
        return catalogo.criarDisciplina(disciplina);
    }

    @PutMapping("/disciplinas/{id}")
    DisciplinaVisao atualizarDisciplina(@PathVariable Long id, @Valid @RequestBody DadosDisciplina disciplina) {
        return catalogo.atualizarDisciplina(id, disciplina);
    }
}
