

public class SistemaPagamentoExterno implements SistemaPagamento {

    @Override
    public void receberAvisoDeMatricula() {
        System.out.println("[SistemaPagamentoExterno] Notificacao de matricula recebida com sucesso.");
    }

    @Override
    public void cobrarAluno() {
        System.out.println("[SistemaPagamentoExterno] Fatura gerada e cobranca enviada ao aluno.");
    }
}
