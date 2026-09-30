package br.edu.matricula.api.repositorio;

import br.edu.matricula.dominio.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNumPessoa(String numPessoa);

    boolean existsByNumPessoa(String numPessoa);

    boolean existsByIdAndAtivoTrue(Long id);
}
