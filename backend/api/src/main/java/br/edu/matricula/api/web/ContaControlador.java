package br.edu.matricula.api.web;

import br.edu.matricula.api.servico.Comandos.EdicaoConta;
import br.edu.matricula.api.servico.Comandos.NovaConta;
import br.edu.matricula.api.servico.ContaServico;
import br.edu.matricula.api.servico.Visoes.ContaVisao;
import br.edu.matricula.dominio.Papel;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contas")
class ContaControlador {

    private final ContaServico contas;

    ContaControlador(ContaServico contas) {
        this.contas = contas;
    }

    @GetMapping
    List<ContaVisao> listar(@RequestParam Papel papel) {
        return contas.listar(papel);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ContaVisao criar(@Valid @RequestBody NovaConta conta) {
        return contas.criar(conta);
    }

    @PutMapping("/{id}")
    ContaVisao atualizar(@PathVariable Long id, @Valid @RequestBody EdicaoConta conta) {
        return contas.atualizar(id, conta);
    }
}
