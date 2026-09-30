package br.edu.matricula.pagamento;

/** O sistema externo não respondeu ou recusou a chamada. Quem chamou decide se tenta de novo. */
public class PagamentoIndisponivelException extends RuntimeException {

    public PagamentoIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public PagamentoIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
