package br.edu.matricula.api.repositorio;

import br.edu.matricula.dominio.Curriculo;
import br.edu.matricula.dominio.EstadoCurriculo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurriculoRepositorio extends JpaRepository<Curriculo, Long> {

    List<Curriculo> findAllByOrderBySemestreDesc();

    boolean existsBySemestre(String semestre);

    Optional<Curriculo> findFirstByEstadoOrderByIdDesc(EstadoCurriculo estado);
}
