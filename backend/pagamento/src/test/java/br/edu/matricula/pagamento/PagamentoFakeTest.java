package br.edu.matricula.pagamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.matricula.pagamento.PagamentoFake.Situacao;
import org.junit.jupiter.api.Test;

class PagamentoFakeTest {

    private static final AvisoDeMatricula AVISO =
            new AvisoDeMatricula("matricula-1", "20261001", "João Pedro", "Redes", "2026/2");

    @Test
    void avisaCobraECancela() {
        var pagamento = new PagamentoFake(false);

        pagamento.receberAvisoDeMatricula(AVISO);
        assertThat(pagamento.situacao("matricula-1")).contains(Situacao.AVISADA);
        pagamento.cobrarAluno("matricula-1");
        assertThat(pagamento.situacao("matricula-1")).contains(Situacao.COBRADA);
        pagamento.cancelarCobranca("matricula-1");
        assertThat(pagamento.situacao("matricula-1")).contains(Situacao.CANCELADA);
    }

    @Test
    void repetirChamadasNaoMudaOResultado() {
        var pagamento = new PagamentoFake(false);
        pagamento.receberAvisoDeMatricula(AVISO);
        pagamento.cobrarAluno("matricula-1");

        pagamento.receberAvisoDeMatricula(AVISO); // não volta para AVISADA
        pagamento.cobrarAluno("matricula-1");

        assertThat(pagamento.situacao("matricula-1")).contains(Situacao.COBRADA);
        pagamento.cancelarCobranca("matricula-1");
        pagamento.cancelarCobranca("matricula-1");
        assertThat(pagamento.situacao("matricula-1")).contains(Situacao.CANCELADA);
    }

    @Test
    void cancelarChaveDesconhecidaNaoEhErro() {
        var pagamento = new PagamentoFake(false);
        pagamento.cancelarCobranca("nao-existe");
        assertThat(pagamento.situacao("nao-existe")).isEmpty();
    }

    @Test
    void cobrarSemAvisoFalha() {
        assertThatThrownBy(() -> new PagamentoFake(false).cobrarAluno("matricula-9"))
                .isInstanceOf(PagamentoIndisponivelException.class);
    }

    @Test
    void indisponivelFalhaEmTodasAsOperacoes() {
        var pagamento = new PagamentoFake(true);

        assertThatThrownBy(() -> pagamento.receberAvisoDeMatricula(AVISO)).isInstanceOf(PagamentoIndisponivelException.class);
        assertThatThrownBy(() -> pagamento.cobrarAluno("matricula-1")).isInstanceOf(PagamentoIndisponivelException.class);
        assertThatThrownBy(() -> pagamento.cancelarCobranca("matricula-1")).isInstanceOf(PagamentoIndisponivelException.class);

        pagamento.indisponivel(false);
        pagamento.receberAvisoDeMatricula(AVISO);
        assertThat(pagamento.situacao("matricula-1")).contains(Situacao.AVISADA);
    }
}
