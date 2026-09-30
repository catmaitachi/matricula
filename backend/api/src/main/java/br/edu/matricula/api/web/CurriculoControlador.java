package br.edu.matricula.api.web;

import br.edu.matricula.api.servico.Comandos.NovaTurma;
import br.edu.matricula.api.servico.Comandos.NovoCurriculo;
import br.edu.matricula.api.servico.CurriculoServico;
import br.edu.matricula.api.servico.Visoes.CurriculoResumo;
import br.edu.matricula.api.servico.Visoes.CurriculoVisao;
import br.edu.matricula.api.servico.Visoes.EncerramentoVisao;
import br.edu.matricula.api.servico.Visoes.TurmaVisao;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/curriculos")
class CurriculoControlador {

    private final CurriculoServico curriculos;

    CurriculoControlador(CurriculoServico curriculos) {
        this.curriculos = curriculos;
    }

    @GetMapping
    List<CurriculoResumo> listar() {
        return curriculos.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CurriculoResumo criar(@Valid @RequestBody NovoCurriculo curriculo) {
        return curriculos.criar(curriculo);
    }

    @GetMapping("/{id}")
    CurriculoVisao detalhe(@PathVariable Long id) {
        return curriculos.detalhe(id);
    }

    @PostMapping("/{id}/turmas")
    @ResponseStatus(HttpStatus.CREATED)
    TurmaVisao adicionarTurma(@PathVariable Long id, @Valid @RequestBody NovaTurma turma) {
        return curriculos.adicionarTurma(id, turma);
    }

    @DeleteMapping("/{id}/turmas/{turmaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removerTurma(@PathVariable Long id, @PathVariable Long turmaId) {
        curriculos.removerTurma(id, turmaId);
    }

    @PostMapping("/{id}/abrir")
    CurriculoResumo abrir(@PathVariable Long id) {
        return curriculos.abrir(id);
    }

    @PostMapping("/{id}/encerrar")
    EncerramentoVisao encerrar(@PathVariable Long id) {
        return curriculos.encerrar(id);
    }
}
