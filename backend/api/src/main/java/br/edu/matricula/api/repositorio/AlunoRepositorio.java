package br.edu.matricula.api.repositorio;

import br.edu.matricula.dominio.Aluno;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlunoRepositorio extends JpaRepository<Aluno, Long> {

    List<Aluno> findAllByOrderByNome();

    boolean existsByNumMatricula(String numMatricula);

    boolean existsByNumMatriculaAndIdNot(String numMatricula, Long id);

    /** Trava a linha do aluno: duas matrículas do mesmo aluno ao mesmo tempo não furam o limite. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Aluno a where a.id = :id")
    Optional<Aluno> buscarParaAtualizar(@Param("id") Long id);
}
