package br.edu.matricula.pagamento;

/**
 * Porta para o sistema de pagamento externo (RNF01). Todas as operações são idempotentes pela {@code chave}:
 * repetir uma chamada (por exemplo, numa nova tentativa) não cobra nem cancela duas vezes.
 */
public interface SistemaPagamento {

    /** Avisa que o aluno se matriculou; o sistema externo passa a conhecer a matrícula. */
    void receberAvisoDeMatricula(AvisoDeMatricula aviso);

    /** Gera a cobrança da matrícula já avisada. */
    void cobrarAluno(String chave);

    /** Cancela a cobrança (matrícula desfeita ou turma cancelada). Chave desconhecida não é erro. */
    void cancelarCobranca(String chave);
}
