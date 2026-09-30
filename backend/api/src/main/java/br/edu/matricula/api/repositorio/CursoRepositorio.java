package br.edu.matricula.api.repositorio;

import br.edu.matricula.dominio.Curso;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepositorio extends JpaRepository<Curso, Long> {

    List<Curso> findAllByOrderByNome();

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);
}
