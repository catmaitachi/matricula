

import java.util.ArrayList;
import java.util.List;

public class Curriculo {
    private String semestre;
    private List<Disciplina> disciplinas;

    public Curriculo() {
        this.disciplinas = new ArrayList<>();
    }

    public Curriculo(String semestre) {
        this.semestre = semestre;
        this.disciplinas = new ArrayList<>();
    }

    public Curriculo(String semestre, List<Disciplina> disciplinas) {
        this.semestre = semestre;
        this.disciplinas = disciplinas;
    }

    public String getSemestre() {
        return semestre;
    }

    public void setSemestre(String semestre) {
        this.semestre = semestre;
    }

    public List<Disciplina> getDisciplinas() {
        return disciplinas;
    }

    public void setDisciplinas(List<Disciplina> disciplinas) {
        this.disciplinas = disciplinas;
    }

    public void adicionarDisciplina(Disciplina disciplina) {
        this.disciplinas.add(disciplina);
    }

    @Override
    public String toString() {
        return "Curriculo{" +
                "semestre='" + semestre + '\'' +
                ", qtdDisciplinas=" + (disciplinas != null ? disciplinas.size() : 0) +
                '}';
    }
}
