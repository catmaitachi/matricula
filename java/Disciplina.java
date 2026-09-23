

import java.util.ArrayList;
import java.util.List;

public class Disciplina {
    private String nome;
    private boolean ativa;
    private int maxAlunos;
    private int minAlunos;

    private List<Curso> cursos;
    private List<Professor> professores;
    private List<Matricula> matriculas;

    public Disciplina() {
        this.cursos = new ArrayList<>();
        this.professores = new ArrayList<>();
        this.matriculas = new ArrayList<>();
        this.ativa = true;
        this.minAlunos = 3;
        this.maxAlunos = 60;
    }

    public Disciplina(String nome, boolean ativa, int maxAlunos, int minAlunos) {
        this.nome = nome;
        this.ativa = ativa;
        this.maxAlunos = maxAlunos;
        this.minAlunos = minAlunos;
        this.cursos = new ArrayList<>();
        this.professores = new ArrayList<>();
        this.matriculas = new ArrayList<>();
    }

    public void validarSeOcorre() {
        System.out.println("[Stub] Disciplina.validarSeOcorre(): Validando se disciplina '" + nome +
                "' possui no minimo " + minAlunos + " alunos matriculados.");
    }

    public void encerrarInscricoes() {
        System.out.println("[Stub] Disciplina.encerrarInscricoes(): Encerrando inscricoes para a disciplina '" + nome + "'.");
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public int getMaxAlunos() {
        return maxAlunos;
    }

    public void setMaxAlunos(int maxAlunos) {
        this.maxAlunos = maxAlunos;
    }

    public int getMinAlunos() {
        return minAlunos;
    }

    public void setMinAlunos(int minAlunos) {
        this.minAlunos = minAlunos;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }

    public List<Professor> getProfessores() {
        return professores;
    }

    public void setProfessores(List<Professor> professores) {
        this.professores = professores;
    }

    public List<Matricula> getMatriculas() {
        return matriculas;
    }

    public void setMatriculas(List<Matricula> matriculas) {
        this.matriculas = matriculas;
    }

    @Override
    public String toString() {
        return "Disciplina{" +
                "nome='" + nome + '\'' +
                ", ativa=" + ativa +
                ", maxAlunos=" + maxAlunos +
                ", minAlunos=" + minAlunos +
                '}';
    }
}
