package br.edu.matricula.api.servico;

import br.edu.matricula.api.repositorio.CurriculoRepositorio;
import br.edu.matricula.api.repositorio.DisciplinaRepositorio;
import br.edu.matricula.api.repositorio.ProfessorRepositorio;
import br.edu.matricula.api.repositorio.TurmaRepositorio;
import br.edu.matricula.api.servico.Comandos.NovaTurma;
import br.edu.matricula.api.servico.Comandos.NovoCurriculo;
import br.edu.matricula.api.servico.Visoes.CurriculoResumo;
import br.edu.matricula.api.servico.Visoes.CurriculoVisao;
import br.edu.matricula.api.servico.Visoes.EncerramentoVisao;
import br.edu.matricula.api.servico.Visoes.TurmaVisao;
import br.edu.matricula.dominio.Curriculo;
import br.edu.matricula.dominio.EstadoCurriculo;
import br.edu.matricula.dominio.Matricula;
import br.edu.matricula.dominio.RegraDeNegocioException;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** RF01: a secretaria gera o currículo do semestre, abre as inscrições e as encerra. */
@Service
public class CurriculoServico {

    private final CurriculoRepositorio curriculos;
    private final TurmaRepositorio turmas;
    private final DisciplinaRepositorio disciplinas;
    private final ProfessorRepositorio professores;
    private final CobrancaServico cobrancas;
    private final TransactionTemplate tx;

    CurriculoServico(CurriculoRepositorio curriculos, TurmaRepositorio turmas, DisciplinaRepositorio disciplinas,
                     ProfessorRepositorio professores, CobrancaServico cobrancas, PlatformTransactionManager gerenciador) {
        this.curriculos = curriculos;
        this.turmas = turmas;
        this.disciplinas = disciplinas;
        this.professores = professores;
        this.cobrancas = cobrancas;
        this.tx = new TransactionTemplate(gerenciador);
    }

    @Transactional(readOnly = true)
    public List<CurriculoResumo> listar() {
        return curriculos.findAllByOrderBySemestreDesc().stream().map(CurriculoResumo::de).toList();
    }

    @Transactional
    public CurriculoResumo criar(NovoCurriculo c) {
        var curriculo = new Curriculo(c.semestre());
        if (curriculos.existsBySemestre(curriculo.semestre())) {
            throw new RegraDeNegocioException("SEMESTRE_EM_USO", "Já existe um currículo para " + curriculo.semestre() + ".");
        }
        return CurriculoResumo.de(curriculos.save(curriculo));
    }

    @Transactional(readOnly = true)
    public CurriculoVisao detalhe(Long id) {
        var curriculo = buscar(id);
        return new CurriculoVisao(curriculo.id(), curriculo.semestre(), curriculo.estado(),
                turmas.comDetalhes(id).stream().map(TurmaVisao::de).toList());
    }

    @Transactional
    public TurmaVisao adicionarTurma(Long curriculoId, NovaTurma n) {
        var curriculo = buscar(curriculoId);
        var disciplina = disciplinas.findById(n.disciplinaId()).orElseThrow(() -> new NaoEncontradoException("Disciplina não encontrada."));
        var professor = professores.findById(n.professorId()).orElseThrow(() -> new NaoEncontradoException("Professor não encontrado."));
        var turma = curriculo.adicionarTurma(disciplina, professor, n.turno());
        curriculos.flush(); // o cascade grava a turma nova e lhe dá o id; save() faria merge e devolveria uma cópia
        return TurmaVisao.de(turma);
    }

    @Transactional
    public void removerTurma(Long curriculoId, Long turmaId) {
        var curriculo = buscar(curriculoId);
        var turma = curriculo.turmas().stream().filter(t -> turmaId.equals(t.id())).findFirst()
                .orElseThrow(() -> new NaoEncontradoException("Turma não encontrada."));
        curriculo.removerTurma(turma);
    }

    @Transactional
    public CurriculoResumo abrir(Long id) {
        var curriculo = buscar(id);
        curriculos.findFirstByEstadoOrderByIdDesc(EstadoCurriculo.ABERTO).filter(outro -> !outro.id().equals(id)).ifPresent(outro -> {
            throw new RegraDeNegocioException("JA_HA_CURRICULO_ABERTO",
                    "O semestre " + outro.semestre() + " ainda está com inscrições abertas. Encerre-o antes.");
        });
        curriculo.abrir();
        return CurriculoResumo.de(curriculo);
    }

    /**
     * Encerra as inscrições. Numa transação: trava as turmas, cancela as que não têm quórum e marca as cobranças
     * a cancelar. Depois, fora dela, avisa o sistema de pagamento; o que falhar é repetido pelo reconciliador.
     */
    public EncerramentoVisao encerrar(Long id) {
        record Saida(int turmasCanceladas, List<Long> matriculas) {
        }
        var saida = Objects.requireNonNull(tx.execute(s -> {
            var curriculo = buscar(id);
            turmas.travarDoCurriculo(id);
            var resultado = curriculo.encerrar();
            return new Saida(resultado.turmasCanceladas().size(), resultado.matriculasCanceladas().stream().map(Matricula::id).toList());
        }));
        saida.matriculas().forEach(cobrancas::cancelar);
        return new EncerramentoVisao(saida.turmasCanceladas(), saida.matriculas().size());
    }

    private Curriculo buscar(Long id) {
        return curriculos.findById(id).orElseThrow(() -> new NaoEncontradoException("Currículo não encontrado."));
    }
}
