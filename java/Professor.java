

import java.util.ArrayList;
import java.util.List;

public class Professor extends Usuario {
    private List<Disciplina> disciplinasLecionadas;

    public Professor() {
        super();
        this.disciplinasLecionadas = new ArrayList<>();
    }

    public Professor(String nome, String numPessoa, String senha) {
        super(nome, numPessoa, senha);
        this.disciplinasLecionadas = new ArrayList<>();
    }

    public void visualizarAlunosMatriculados(int aluno) {
        System.out.println("[Stub] Professor.visualizarAlunosMatriculados(): Professor " + getNome() +
                " visualizando alunos matriculados referente ao ID: " + aluno);
    }

    public void visualizarAlunosMatriculados(Disciplina disciplina) {
        System.out.println("[Stub] Professor.visualizarAlunosMatriculados(): Professor " + getNome() +
                " visualizando alunos da disciplina: " + (disciplina != null ? disciplina.getNome() : "null"));
    }

    public List<Disciplina> getDisciplinasLecionadas() {
        return disciplinasLecionadas;
    }

    public void setDisciplinasLecionadas(List<Disciplina> disciplinasLecionadas) {
        this.disciplinasLecionadas = disciplinasLecionadas;
    }

    public void adicionarDisciplina(Disciplina disciplina) {
        this.disciplinasLecionadas.add(disciplina);
    }

    @Override
    public String toString() {
        return "Professor{" +
                "nome='" + getNome() + '\'' +
                ", numPessoa='" + getNumPessoa() + '\'' +
                '}';
    }
}
