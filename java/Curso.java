

import java.util.ArrayList;
import java.util.List;

public class Curso {
    private String nome;
    private int numCreditos;
    private List<Disciplina> disciplinas;

    public Curso() {
        this.disciplinas = new ArrayList<>();
    }

    public Curso(String nome, int numCreditos) {
        this.nome = nome;
        this.numCreditos = numCreditos;
        this.disciplinas = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getNumCreditos() {
        return numCreditos;
    }

    public void setNumCreditos(int numCreditos) {
        this.numCreditos = numCreditos;
    }

    public List<Disciplina> getDisciplinas() {
        return disciplinas;
    }

    public void setDisciplinas(List<Disciplina> disciplinas) {
        this.disciplinas = disciplinas;
    }

    public void adicionarDisciplina(Disciplina disciplina) {
        if (!this.disciplinas.contains(disciplina)) {
            this.disciplinas.add(disciplina);
        }
    }

    @Override
    public String toString() {
        return "Curso{" +
                "nome='" + nome + '\'' +
                ", numCreditos=" + numCreditos +
                '}';
    }
}
