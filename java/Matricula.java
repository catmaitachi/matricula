

public class Matricula {
    private String tipo;
    private Aluno aluno;
    private Disciplina disciplina;
    private SistemaPagamento sistemaPagamento;

    public Matricula() {
    }

    public Matricula(String tipo, Aluno aluno, Disciplina disciplina) {
        this.tipo = tipo;
        this.aluno = aluno;
        this.disciplina = disciplina;
    }

    public Matricula(String tipo, Aluno aluno, Disciplina disciplina, SistemaPagamento sistemaPagamento) {
        this.tipo = tipo;
        this.aluno = aluno;
        this.disciplina = disciplina;
        this.sistemaPagamento = sistemaPagamento;
    }

    public void notificarSistemaPagamento() {
        if (sistemaPagamento != null) {
            System.out.println("[Stub] Matricula.notificarSistemaPagamento(): Enviando aviso de cobranca...");
            sistemaPagamento.receberAvisoDeMatricula();
            sistemaPagamento.cobrarAluno();
        } else {
            System.out.println("[Stub] Matricula.notificarSistemaPagamento(): Nenhum sistema de pagamento configurado.");
        }
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    public SistemaPagamento getSistemaPagamento() {
        return sistemaPagamento;
    }

    public void setSistemaPagamento(SistemaPagamento sistemaPagamento) {
        this.sistemaPagamento = sistemaPagamento;
    }

    @Override
    public String toString() {
        return "Matricula{" +
                "tipo='" + tipo + '\'' +
                ", aluno=" + (aluno != null ? aluno.getNome() : "null") +
                ", disciplina=" + (disciplina != null ? disciplina.getNome() : "null") +
                '}';
    }
}
