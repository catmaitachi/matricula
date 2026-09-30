package br.edu.matricula.pagamento;

/** Dados que o sistema externo recebe sobre a matrícula. Só o necessário para cobrar. */
public record AvisoDeMatricula(String chave, String numMatricula, String aluno, String disciplina, String semestre) {
}
