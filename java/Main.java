

public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  SISTEMA DE MATRICULA - DEMONSTRACAO DOS STUBS  ");
        System.out.println("=================================================");

        Secretaria secretaria = new Secretaria("Maria Silva", "SEC001", "senhaSec123");
        secretaria.realizarLogin();
        secretaria.manterAlunos();
        secretaria.manterProfessores();
        secretaria.manterDisciplinas();

        Curriculo curriculo = secretaria.gerarCurriculoSemestre();
        System.out.println("Curriculo gerado: " + curriculo);

        Curso curso = new Curso("Engenharia de Software", 240);
        Disciplina disciplina = new Disciplina("Laboratorio de Desenvolvimento de Software", true, 40, 3);
        curso.adicionarDisciplina(disciplina);
        curriculo.adicionarDisciplina(disciplina);

        disciplina.validarSeOcorre();
        disciplina.encerrarInscricoes();

        Professor professor = new Professor("Dr. Carlos Eduardo", "PROF100", "senhaProf456");
        professor.realizarLogin();
        professor.adicionarDisciplina(disciplina);
        professor.visualizarAlunosMatriculados(101);
        professor.visualizarAlunosMatriculados(disciplina);

        Aluno aluno = new Aluno("Joao Pedro", "ALU999", "senhaAlu789", "20261001");
        aluno.realizarLogin();
        aluno.matricularEmDisciplina(101);
        aluno.matricularEmDisciplina(disciplina);

        SistemaPagamento sistemaPagamento = new SistemaPagamentoExterno();
        Matricula matricula = new Matricula("OBRIGATORIA", aluno, disciplina, sistemaPagamento);
        aluno.adicionarMatricula(matricula);
        disciplina.getMatriculas().add(matricula);

        matricula.notificarSistemaPagamento();

        aluno.cancelarMatricula();

        System.out.println("=================================================");
        System.out.println("  EXECUCAO DOS STUBS CONCLUIDA COM SUCESSO!      ");
        System.out.println("=================================================");
    }
}
