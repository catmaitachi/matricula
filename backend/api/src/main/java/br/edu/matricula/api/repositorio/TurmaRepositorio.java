package br.edu.matricula.api.repositorio;

import br.edu.matricula.dominio.EstadoCurriculo;
import br.edu.matricula.dominio.Turma;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TurmaRepositorio extends JpaRepository<Turma, Long> {

    @Query("""
            select distinct t from Turma t
            join fetch t.disciplina join fetch t.professor left join fetch t.matriculas
            where t.curriculo.id = :curriculoId order by t.id""")
    List<Turma> comDetalhes(@Param("curriculoId") Long curriculoId);

    @Query("""
            select distinct t from Turma t
            join fetch t.curriculo join fetch t.disciplina left join fetch t.matriculas
            where t.professor.id = :professorId
              and t.curriculo.estado <> br.edu.matricula.dominio.EstadoCurriculo.RASCUNHO
            order by t.curriculo.semestre desc, t.disciplina.nome, t.codigo""")
    List<Turma> doProfessor(@Param("professorId") Long professorId);

    boolean existsByDisciplinaIdAndCurriculoEstado(Long disciplinaId, EstadoCurriculo estado);

    /** Trava a turma: as matrículas na última vaga acontecem uma de cada vez. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Turma t where t.id = :id")
    Optional<Turma> buscarParaAtualizar(@Param("id") Long id);

    /** Trava todas as turmas do currículo enquanto ele é encerrado. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Turma t where t.curriculo.id = :curriculoId order by t.id")
    List<Turma> travarDoCurriculo(@Param("curriculoId") Long curriculoId);
}
