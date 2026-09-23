

import java.util.ArrayList;
import java.util.List;

public class Secretaria extends Usuario {
    private List<Curriculo> curriculosGerados;

    public Secretaria() {
        super();
        this.curriculosGerados = new ArrayList<>();
    }

    public Secretaria(String nome, String numPessoa, String senha) {
        super(nome, numPessoa, senha);
        this.curriculosGerados = new ArrayList<>();
    }

    public void manterAlunos() {
        System.out.println("[Stub] Secretaria.manterAlunos(): Gerenciando cadastros de alunos.");
    }

    public void manterProfessores() {
        System.out.println("[Stub] Secretaria.manterProfessores(): Gerenciando cadastros de professores.");
    }

    public void manterDisciplinas() {
        System.out.println("[Stub] Secretaria.manterDisciplinas(): Gerenciando cadastro e oferta de disciplinas.");
    }

    public Curriculo gerarCurriculoSemestre() {
        System.out.println("[Stub] Secretaria.gerarCurriculoSemestre(): Gerando curriculo para o semestre atual.");
        Curriculo novoCurriculo = new Curriculo("2026/2");
        this.curriculosGerados.add(novoCurriculo);
        return novoCurriculo;
    }

    public List<Curriculo> getCurriculosGerados() {
        return curriculosGerados;
    }

    public void setCurriculosGerados(List<Curriculo> curriculosGerados) {
        this.curriculosGerados = curriculosGerados;
    }

    @Override
    public String toString() {
        return "Secretaria{" +
                "nome='" + getNome() + '\'' +
                ", numPessoa='" + getNumPessoa() + '\'' +
                '}';
    }
}
