package br.edu.matricula.api.servico;

import br.edu.matricula.api.repositorio.CursoRepositorio;
import br.edu.matricula.api.repositorio.DisciplinaRepositorio;
import br.edu.matricula.api.repositorio.TurmaRepositorio;
import br.edu.matricula.api.servico.Comandos.DadosCurso;
import br.edu.matricula.api.servico.Comandos.DadosDisciplina;
import br.edu.matricula.api.servico.Visoes.CursoVisao;
import br.edu.matricula.api.servico.Visoes.DisciplinaVisao;
import br.edu.matricula.dominio.Curso;
import br.edu.matricula.dominio.Disciplina;
import br.edu.matricula.dominio.EstadoCurriculo;
import br.edu.matricula.dominio.RegraDeNegocioException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cursos e disciplinas: o catálogo de onde a secretaria monta o currículo de cada semestre. */
@Service
@Transactional
public class CatalogoServico {

    private final CursoRepositorio cursos;
    private final DisciplinaRepositorio disciplinas;
    private final TurmaRepositorio turmas;

    CatalogoServico(CursoRepositorio cursos, DisciplinaRepositorio disciplinas, TurmaRepositorio turmas) {
        this.cursos = cursos;
        this.disciplinas = disciplinas;
        this.turmas = turmas;
    }

    @Transactional(readOnly = true)
    public List<CursoVisao> cursos() {
        return cursos.findAllByOrderByNome().stream().map(CursoVisao::de).toList();
    }

    public CursoVisao criarCurso(DadosCurso d) {
        if (cursos.existsByNome(d.nome().strip())) {
            throw new RegraDeNegocioException("NOME_EM_USO", "Já existe um curso com este nome.");
        }
        return CursoVisao.de(cursos.save(new Curso(d.nome(), d.numCreditos())));
    }

    public CursoVisao atualizarCurso(Long id, DadosCurso d) {
        var curso = cursos.findById(id).orElseThrow(() -> new NaoEncontradoException("Curso não encontrado."));
        if (cursos.existsByNomeAndIdNot(d.nome().strip(), id)) {
            throw new RegraDeNegocioException("NOME_EM_USO", "Já existe um curso com este nome.");
        }
        curso.alterar(d.nome(), d.numCreditos());
        return CursoVisao.de(curso);
    }

    @Transactional(readOnly = true)
    public List<DisciplinaVisao> disciplinas() {
        return disciplinas.todasComCursos().stream().map(DisciplinaVisao::de).toList();
    }

    public DisciplinaVisao criarDisciplina(DadosDisciplina d) {
        if (disciplinas.existsByNome(d.nome().strip())) {
            throw new RegraDeNegocioException("NOME_EM_USO", "Já existe uma disciplina com este nome.");
        }
        var disciplina = new Disciplina(d.nome(), d.minAlunos(), d.maxAlunos(), buscarCursos(d.cursoIds()));
        disciplina.ativa(d.ativa());
        return DisciplinaVisao.de(disciplinas.save(disciplina));
    }

    public DisciplinaVisao atualizarDisciplina(Long id, DadosDisciplina d) {
        var disciplina = disciplinas.findById(id).orElseThrow(() -> new NaoEncontradoException("Disciplina não encontrada."));
        if (disciplinas.existsByNomeAndIdNot(d.nome().strip(), id)) {
            throw new RegraDeNegocioException("NOME_EM_USO", "Já existe uma disciplina com este nome.");
        }
        var mudouLimites = d.minAlunos() != disciplina.minAlunos() || d.maxAlunos() != disciplina.maxAlunos();
        if (mudouLimites && turmas.existsByDisciplinaIdAndCurriculoEstado(id, EstadoCurriculo.ABERTO)) {
            throw new RegraDeNegocioException("DISCIPLINA_EM_USO",
                    "Não é possível mudar os limites de alunos enquanto há inscrições abertas com esta disciplina.");
        }
        disciplina.alterar(d.nome(), d.minAlunos(), d.maxAlunos(), buscarCursos(d.cursoIds()));
        disciplina.ativa(d.ativa());
        return DisciplinaVisao.de(disciplina);
    }

    private Set<Curso> buscarCursos(Set<Long> ids) {
        var encontrados = new HashSet<>(cursos.findAllById(ids));
        if (encontrados.size() != ids.size()) {
            throw new NaoEncontradoException("Algum dos cursos informados não existe.");
        }
        return encontrados;
    }
}
