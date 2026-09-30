package br.edu.matricula.api.repositorio;

import br.edu.matricula.dominio.Professor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepositorio extends JpaRepository<Professor, Long> {

    List<Professor> findAllByOrderByNome();
}
