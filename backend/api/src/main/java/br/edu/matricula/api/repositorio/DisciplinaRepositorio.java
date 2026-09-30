package br.edu.matricula.api.repositorio;

import br.edu.matricula.dominio.Disciplina;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DisciplinaRepositorio extends JpaRepository<Disciplina, Long> {

    @Query("select distinct d from Disciplina d left join fetch d.cursos order by d.nome")
    List<Disciplina> todasComCursos();

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);
}
