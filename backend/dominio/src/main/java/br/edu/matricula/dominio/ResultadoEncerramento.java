package br.edu.matricula.dominio;

import java.util.List;

/** O que o encerramento das inscrições de um semestre produziu. */
public record ResultadoEncerramento(List<Turma> turmasCanceladas, List<Matricula> matriculasCanceladas) {
}
