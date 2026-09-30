package br.edu.matricula.api.servico;

import br.edu.matricula.api.repositorio.AlunoRepositorio;
import br.edu.matricula.api.repositorio.CurriculoRepositorio;
import br.edu.matricula.api.repositorio.MatriculaRepositorio;
import br.edu.matricula.api.repositorio.TurmaRepositorio;
import br.edu.matricula.api.servico.Comandos.NovaMatricula;
import br.edu.matricula.api.servico.Visoes.CurriculoParaAluno;
import br.edu.matricula.api.servico.Visoes.MatriculaVisao;
import br.edu.matricula.api.servico.Visoes.MinhasMatriculas;
import br.edu.matricula.api.servico.Visoes.TurmaParaAluno;
import br.edu.matricula.dominio.Curriculo;
import br.edu.matricula.dominio.EstadoCurriculo;
import br.edu.matricula.dominio.EstadoMatricula;
import br.edu.matricula.dominio.Matricula;
import br.edu.matricula.dominio.TipoMatricula;
import br.edu.matricula.pagamento.PagamentoIndisponivelException;
import br.edu.matricula.pagamento.SistemaPagamento;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** RF02 (o aluno mantém a matrícula) e RF05 (a matrícula gera a cobrança no sistema externo). */
@Service
public class MatriculaServico {

    private final AlunoRepositorio alunos;
    private final TurmaRepositorio turmas;
    private final MatriculaRepositorio matriculas;
    private final CurriculoRepositorio curriculos;
    private final CobrancaServico cobrancas;
    private final SistemaPagamento pagamento;
    private final TransactionTemplate tx;

    MatriculaServico(AlunoRepositorio alunos, TurmaRepositorio turmas, MatriculaRepositorio matriculas,
                     CurriculoRepositorio curriculos, CobrancaServico cobrancas, SistemaPagamento pagamento,
                     PlatformTransactionManager gerenciador) {
        this.alunos = alunos;
        this.turmas = turmas;
        this.matriculas = matriculas;
        this.curriculos = curriculos;
        this.cobrancas = cobrancas;
        this.pagamento = pagamento;
        this.tx = new TransactionTemplate(gerenciador);
    }

    /**
     * Matricula e cobra. Passo 1 (transação, com trava no aluno e na turma): valida as regras e grava a matrícula
     * com cobrança pendente. Passo 2 (fora da transação): avisa e cobra no sistema externo. Se ele falhar, a
     * matrícula é desfeita e o erro sobe (503): sem cobrança, não há matrícula.
     */
    public MatriculaVisao matricular(Long alunoId, NovaMatricula pedido) {
        var id = Objects.requireNonNull(tx.execute(s -> {
            var aluno = alunos.buscarParaAtualizar(alunoId).orElseThrow(() -> new NaoEncontradoException("Aluno não encontrado."));
            var turma = turmas.buscarParaAtualizar(pedido.turmaId()).orElseThrow(() -> new NaoEncontradoException("Turma não encontrada."));
            return matriculas.saveAndFlush(aluno.matricular(turma, pedido.tipo())).id();
        }));
        try {
            cobrancas.enviar(id);
        } catch (PagamentoIndisponivelException e) {
            tx.executeWithoutResult(s -> matriculas.deleteById(id));
            try {
                pagamento.cancelarCobranca(Matricula.chaveDeCobranca(id)); // se o aviso chegou a sair, limpa do outro lado
            } catch (PagamentoIndisponivelException ignorada) {
                // sem resposta do outro lado: nada a fazer
            }
            throw e;
        }
        return Objects.requireNonNull(tx.execute(s -> MatriculaVisao.de(matriculas.findById(id).orElseThrow())));
    }

    /** Desfaz a matrícula e cancela a cobrança. Se o sistema externo falhar, nada muda e o erro sobe. */
    public void cancelar(Long alunoId, Long matriculaId) {
        tx.executeWithoutResult(s -> {
            var aluno = alunos.buscarParaAtualizar(alunoId).orElseThrow(() -> new NaoEncontradoException("Aluno não encontrado."));
            // outra pessoa recebe o mesmo "não encontrada" de quem pede uma matrícula que não existe
            var matricula = matriculas.findById(matriculaId)
                    .filter(m -> m.aluno().id().equals(alunoId))
                    .orElseThrow(() -> new NaoEncontradoException("Matrícula não encontrada."));
            turmas.buscarParaAtualizar(matricula.turma().id());
            aluno.cancelar(matricula);
            matriculas.delete(matricula);
            pagamento.cancelarCobranca(matricula.chaveDeCobranca());
        });
    }

    @Transactional(readOnly = true)
    public CurriculoParaAluno curriculo(Long alunoId) {
        var curriculo = curriculos.findFirstByEstadoOrderByIdDesc(EstadoCurriculo.ABERTO)
                .orElseThrow(() -> new NaoEncontradoException("SEM_CURRICULO_ABERTO", "Não há inscrições abertas no momento."));
        var minhas = new HashMap<Long, Long>();
        matriculas.doAlunoNoCurriculo(alunoId, curriculo.id()).stream()
                .filter(Matricula::ativa)
                .forEach(m -> minhas.put(m.turma().id(), m.id()));
        var visao = turmas.comDetalhes(curriculo.id()).stream()
                .map(t -> new TurmaParaAluno(t.id(), t.disciplina().nome(), t.codigo(), t.turno(), t.professor().nome(),
                        t.estado(), t.matriculasAtivas().size(), t.disciplina().minAlunos(), t.disciplina().maxAlunos(),
                        minhas.get(t.id())))
                .toList();
        return new CurriculoParaAluno(curriculo.semestre(), curriculo.estado(), visao);
    }

    @Transactional(readOnly = true)
    public MinhasMatriculas minhas(Long alunoId) {
        var vazio = new MinhasMatriculas(null, null, 0, TipoMatricula.OBRIGATORIA.limite(), 0, TipoMatricula.OPTATIVA.limite(), List.of());
        Optional<Curriculo> atual = curriculos.findFirstByEstadoOrderByIdDesc(EstadoCurriculo.ABERTO)
                .or(() -> curriculos.findFirstByEstadoOrderByIdDesc(EstadoCurriculo.ENCERRADO));
        if (atual.isEmpty()) {
            return vazio;
        }
        var doAluno = matriculas.doAlunoNoCurriculo(alunoId, atual.get().id());
        var ativas = doAluno.stream().filter(m -> m.estado() == EstadoMatricula.ATIVA).toList();
        return new MinhasMatriculas(atual.get().semestre(), atual.get().estado(),
                (int) ativas.stream().filter(m -> m.tipo() == TipoMatricula.OBRIGATORIA).count(), TipoMatricula.OBRIGATORIA.limite(),
                (int) ativas.stream().filter(m -> m.tipo() == TipoMatricula.OPTATIVA).count(), TipoMatricula.OPTATIVA.limite(),
                doAluno.stream().map(MatriculaVisao::de).toList());
    }
}
