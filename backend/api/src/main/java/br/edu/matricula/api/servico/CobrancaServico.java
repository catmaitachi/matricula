package br.edu.matricula.api.servico;

import br.edu.matricula.api.repositorio.MatriculaRepositorio;
import br.edu.matricula.dominio.Matricula;
import br.edu.matricula.dominio.StatusCobranca;
import br.edu.matricula.pagamento.AvisoDeMatricula;
import br.edu.matricula.pagamento.PagamentoIndisponivelException;
import br.edu.matricula.pagamento.SistemaPagamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Conversa com o sistema de pagamento externo. As chamadas de rede ficam fora das transações do banco
 * (não seguram trava enquanto esperam) e são idempotentes, então podem ser repetidas.
 */
@Service
public class CobrancaServico {

    private static final Logger log = LoggerFactory.getLogger(CobrancaServico.class);

    private final MatriculaRepositorio matriculas;
    private final SistemaPagamento pagamento;
    private final TransactionTemplate tx;

    CobrancaServico(MatriculaRepositorio matriculas, SistemaPagamento pagamento, PlatformTransactionManager gerenciador) {
        this.matriculas = matriculas;
        this.pagamento = pagamento;
        this.tx = new TransactionTemplate(gerenciador);
    }

    /** Avisa e cobra a matrícula. Não faz nada se ela já foi cobrada ou cancelada. Lança se o sistema externo falhar. */
    public void enviar(Long matriculaId) {
        var aviso = tx.execute(s -> matriculas.findById(matriculaId)
                .filter(m -> m.ativa() && m.cobranca() == StatusCobranca.PENDENTE)
                .map(CobrancaServico::aviso)
                .orElse(null));
        if (aviso == null) {
            return;
        }
        pagamento.receberAvisoDeMatricula(aviso);
        pagamento.cobrarAluno(aviso.chave());
        tx.executeWithoutResult(s -> matriculas.findById(matriculaId).ifPresent(m -> {
            if (m.cobranca() == StatusCobranca.PENDENTE) {
                m.cobrancaEnviada();
            } else {
                // a matrícula caiu enquanto a cobrança saía: desfaz a cobrança que acabou de sair
                pagamento.cancelarCobranca(m.chaveDeCobranca());
            }
        }));
    }

    /** Cancela a cobrança de uma matrícula que caiu. Se o sistema externo falhar, fica pendente para o reconciliador. */
    public void cancelar(Long matriculaId) {
        try {
            pagamento.cancelarCobranca(Matricula.chaveDeCobranca(matriculaId));
            tx.executeWithoutResult(s -> matriculas.findById(matriculaId).ifPresent(Matricula::cobrancaCancelada));
        } catch (PagamentoIndisponivelException e) {
            log.warn("Cancelamento da cobrança da matrícula {} ficou pendente: {}", matriculaId, e.getMessage());
        }
    }

    private static AvisoDeMatricula aviso(Matricula m) {
        var turma = m.turma();
        return new AvisoDeMatricula(m.chaveDeCobranca(), m.aluno().numMatricula(), m.aluno().nome(),
                turma.disciplina().nome(), turma.curriculo().semestre());
    }
}
