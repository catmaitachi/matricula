

import java.util.ArrayList;
import java.util.List;

public class Aluno extends Usuario {
    private String numMatricula;
    private List<Matricula> matriculas;

    public Aluno() {
        super();
        this.matriculas = new ArrayList<>();
    }

    public Aluno(String nome, String numPessoa, String senha, String numMatricula) {
        super(nome, numPessoa, senha);
        this.numMatricula = numMatricula;
        this.matriculas = new ArrayList<>();
    }

    public void matricularEmDisciplina(int disciplina) {
        System.out.println("[Stub] Aluno.matricularEmDisciplina(): Aluno " + getNome() +
                " matriculando-se na disciplina ID: " + disciplina);
    }

    public void matricularEmDisciplina(Disciplina disciplina) {
        System.out.println("[Stub] Aluno.matricularEmDisciplina(): Aluno " + getNome() +
                " matriculando-se na disciplina: " + (disciplina != null ? disciplina.getNome() : "null"));
    }

    public void cancelarMatricula() {
        System.out.println("[Stub] Aluno.cancelarMatricula(): Cancelando matricula do aluno " + getNome());
    }

    public String getNumMatricula() {
        return numMatricula;
    }

    public void setNumMatricula(String numMatricula) {
        this.numMatricula = numMatricula;
    }

    public List<Matricula> getMatriculas() {
        return matriculas;
    }

    public void setMatriculas(List<Matricula> matriculas) {
        this.matriculas = matriculas;
    }

    public void adicionarMatricula(Matricula matricula) {
        if (this.matriculas.size() < 6) {
            this.matriculas.add(matricula);
        } else {
            System.out.println("[Alerta] Aluno já atingiu o limite maximo de 6 disciplinas matriculadas.");
        }
    }

    @Override
    public String toString() {
        return "Aluno{" +
                "nome='" + getNome() + '\'' +
                ", numMatricula='" + numMatricula + '\'' +
                '}';
    }
}
