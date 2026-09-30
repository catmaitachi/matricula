package br.edu.matricula.api.repositorio;

import br.edu.matricula.dominio.Matricula;
import br.edu.matricula.dominio.StatusCobranca;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatriculaRepositorio extends JpaRepository<Matricula, Long> {

    List<Matricula> findByCobrancaAndCriadaEmBefore(StatusCobranca cobranca, Instant antes);

    List<Matricula> findByCobranca(StatusCobranca cobranca);

    @Query("""
            select m from Matricula m join fetch m.turma t join fetch t.disciplina
            where m.aluno.id = :alunoId and t.curriculo.id = :curriculoId order by m.id""")
    List<Matricula> doAlunoNoCurriculo(@Param("alunoId") Long alunoId, @Param("curriculoId") Long curriculoId);

    @Query("""
            select m from Matricula m join fetch m.aluno a
            where m.turma.id = :turmaId and m.estado = br.edu.matricula.dominio.EstadoMatricula.ATIVA
            order by a.nome""")
    List<Matricula> ativasDaTurma(@Param("turmaId") Long turmaId);
}
