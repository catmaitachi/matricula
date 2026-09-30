package br.edu.matricula.dominio;

import java.util.Set;

/** Monta objetos de domínio para os testes, sem banco. */
final class Cenario {

    private Cenario() {
    }

    static Disciplina disciplina(String nome, int min, int max) {
        return new Disciplina(nome, min, max, Set.of());
    }

    static Professor professor() {
        return new Professor("PROF100", "Dr. Carlos", "hash");
    }

    static Aluno aluno(int numero) {
        return new Aluno("ALU" + numero, "Aluno " + numero, "hash", "2026" + numero);
    }

    /** Currículo 2026/2 com uma turma A da disciplina, já aberto. */
    static Turma turmaAberta(Disciplina disciplina) {
        var curriculo = new Curriculo("2026/2");
        var turma = curriculo.adicionarTurma(disciplina, professor(), Turno.NOITE);
        curriculo.abrir();
        return turma;
    }
}
