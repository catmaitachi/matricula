package br.edu.matricula.api.servico;

import br.edu.matricula.api.repositorio.MatriculaRepositorio;
import br.edu.matricula.dominio.Matricula;
import br.edu.matricula.dominio.StatusCobranca;
import br.edu.matricula.pagamento.PagamentoIndisponivelException;
import java.time.Clock;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Repete o que ficou pela metade: cobranças que não saíram (queda no meio) e cancelamentos que o sistema externo recusou. */
@Component
public class ReconciliadorDeCobrancas {

    private static final Logger log = LoggerFactory.getLogger(ReconciliadorDeCobrancas.class);
    private static final Duration IDADE_MINIMA = Duration.ofMinutes(2);

    private final MatriculaRepositorio matriculas;
    private final CobrancaServico cobrancas;
    private final Clock relogio;

    ReconciliadorDeCobrancas(MatriculaRepositorio matriculas, CobrancaServico cobrancas, Clock relogio) {
        this.matriculas = matriculas;
        this.cobrancas = cobrancas;
        this.relogio = relogio;
    }

    @Scheduled(fixedDelayString = "${matricula.pagamento.reconciliacao-ms}")
    void reconciliarPeriodicamente() {
        reconciliar(IDADE_MINIMA);
    }

    /** Só mexe em cobranças pendentes há mais que {@code idadeMinima}, para não competir com a matrícula em andamento. */
    public void reconciliar(Duration idadeMinima) {
        for (var m : matriculas.findByCobrancaAndCriadaEmBefore(StatusCobranca.PENDENTE, relogio.instant().minus(idadeMinima))) {
            try {
                cobrancas.enviar(m.id());
            } catch (PagamentoIndisponivelException e) {
                log.debug("Cobrança da matrícula {} segue pendente: {}", m.id(), e.getMessage());
            }
        }
        matriculas.findByCobranca(StatusCobranca.CANCELAMENTO_PENDENTE).stream().map(Matricula::id).forEach(cobrancas::cancelar);
    }
}
