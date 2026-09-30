package br.edu.matricula.api.servico;

import br.edu.matricula.dominio.Papel;
import br.edu.matricula.dominio.TipoMatricula;
import br.edu.matricula.dominio.Turno;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

/** Entradas da API. Todo texto tem tamanho máximo; o que passa daqui já chega validado aos serviços. */
public final class Comandos {

    private Comandos() {
    }

    public record NovaConta(
            @NotNull Papel papel,
            @NotBlank @Size(max = 30) @Pattern(regexp = "[A-Za-z0-9._-]+", message = "use só letras, números, ponto, hífen e sublinhado") String numPessoa,
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Size(min = 8, max = 72) String senha,
            @Size(max = 20) String numMatricula) {
    }

    public record EdicaoConta(
            @NotBlank @Size(max = 120) String nome,
            boolean ativo,
            @Size(max = 20) String numMatricula,
            @Size(min = 8, max = 72) String novaSenha) {
    }

    public record DadosCurso(@NotBlank @Size(max = 120) String nome, @Min(1) @Max(2000) int numCreditos) {
    }

    public record DadosDisciplina(
            @NotBlank @Size(max = 120) String nome,
            @Min(1) @Max(500) int minAlunos,
            @Min(1) @Max(500) int maxAlunos,
            boolean ativa,
            @NotNull Set<Long> cursoIds) {
    }

    public record NovoCurriculo(@NotBlank @Size(max = 10) String semestre) {
    }

    public record NovaTurma(@NotNull Long disciplinaId, @NotNull Long professorId, @NotNull Turno turno) {
    }

    public record NovaMatricula(@NotNull Long turmaId, @NotNull TipoMatricula tipo) {
    }
}
