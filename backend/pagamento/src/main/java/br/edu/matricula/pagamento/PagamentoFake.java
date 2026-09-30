package br.edu.matricula.pagamento;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Sistema de pagamento simulado, em memória. Serve ao desenvolvimento e aos testes; um cliente HTTP real implementa a mesma porta. */
public class PagamentoFake implements SistemaPagamento {

    public enum Situacao { AVISADA, COBRADA, CANCELADA }

    private final Map<String, Situacao> cobrancas = new ConcurrentHashMap<>();
    private volatile boolean indisponivel;

    public PagamentoFake(boolean indisponivel) {
        this.indisponivel = indisponivel;
    }

    public void indisponivel(boolean indisponivel) {
        this.indisponivel = indisponivel;
    }

    public Optional<Situacao> situacao(String chave) {
        return Optional.ofNullable(cobrancas.get(chave));
    }

    @Override
    public void receberAvisoDeMatricula(AvisoDeMatricula aviso) {
        exigirDisponivel();
        cobrancas.putIfAbsent(aviso.chave(), Situacao.AVISADA);
    }

    @Override
    public void cobrarAluno(String chave) {
        exigirDisponivel();
        var atual = cobrancas.get(chave);
        if (atual == null) {
            throw new PagamentoIndisponivelException("Matrícula desconhecida no sistema de pagamento: " + chave);
        }
        if (atual == Situacao.AVISADA) {
            cobrancas.replace(chave, Situacao.AVISADA, Situacao.COBRADA);
        }
    }

    @Override
    public void cancelarCobranca(String chave) {
        exigirDisponivel();
        cobrancas.computeIfPresent(chave, (k, v) -> Situacao.CANCELADA);
    }

    private void exigirDisponivel() {
        if (indisponivel) {
            throw new PagamentoIndisponivelException("Sistema de pagamento indisponível.");
        }
    }
}
