package br.edu.matricula.api.servico;

import br.edu.matricula.api.repositorio.AlunoRepositorio;
import br.edu.matricula.api.repositorio.ProfessorRepositorio;
import br.edu.matricula.api.repositorio.UsuarioRepositorio;
import br.edu.matricula.api.servico.Comandos.EdicaoConta;
import br.edu.matricula.api.servico.Comandos.NovaConta;
import br.edu.matricula.api.servico.Visoes.ContaVisao;
import br.edu.matricula.dominio.Aluno;
import br.edu.matricula.dominio.Papel;
import br.edu.matricula.dominio.Professor;
import br.edu.matricula.dominio.RegraDeNegocioException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF06: a secretaria mantém as contas de alunos e professores. Contas não são apagadas, só desativadas. */
@Service
public class ContaServico {

    private final UsuarioRepositorio usuarios;
    private final AlunoRepositorio alunos;
    private final ProfessorRepositorio professores;
    private final PasswordEncoder codificador;

    ContaServico(UsuarioRepositorio usuarios, AlunoRepositorio alunos, ProfessorRepositorio professores, PasswordEncoder codificador) {
        this.usuarios = usuarios;
        this.alunos = alunos;
        this.professores = professores;
        this.codificador = codificador;
    }

    @Transactional(readOnly = true)
    public List<ContaVisao> listar(Papel papel) {
        return switch (papel) {
            case ALUNO -> alunos.findAllByOrderByNome().stream().map(ContaVisao::de).toList();
            case PROFESSOR -> professores.findAllByOrderByNome().stream().map(ContaVisao::de).toList();
            case SECRETARIA -> throw new RegraDeNegocioException("PAPEL_NAO_GERENCIAVEL", "Só contas de alunos e professores são mantidas aqui.");
        };
    }

    @Transactional
    public ContaVisao criar(NovaConta c) {
        if (usuarios.existsByNumPessoa(c.numPessoa().strip())) {
            throw new RegraDeNegocioException("NUM_PESSOA_EM_USO", "Já existe uma conta com este nº de pessoa.");
        }
        var hash = hash(c.senha());
        return switch (c.papel()) {
            case ALUNO -> {
                var numMatricula = exigirMatricula(c.numMatricula());
                if (alunos.existsByNumMatricula(numMatricula)) {
                    throw new RegraDeNegocioException("NUM_MATRICULA_EM_USO", "Já existe um aluno com este nº de matrícula.");
                }
                yield ContaVisao.de(alunos.save(new Aluno(c.numPessoa(), c.nome(), hash, numMatricula)));
            }
            case PROFESSOR -> ContaVisao.de(professores.save(new Professor(c.numPessoa(), c.nome(), hash)));
            case SECRETARIA -> throw new RegraDeNegocioException("PAPEL_NAO_GERENCIAVEL", "Só contas de alunos e professores são criadas aqui.");
        };
    }

    @Transactional
    public ContaVisao atualizar(Long id, EdicaoConta e) {
        var usuario = usuarios.findById(id)
                .filter(u -> u.papel() != Papel.SECRETARIA)
                .orElseThrow(() -> new NaoEncontradoException("Conta não encontrada."));
        usuario.alterarNome(e.nome());
        usuario.ativo(e.ativo());
        if (usuario instanceof Aluno aluno) {
            var numMatricula = exigirMatricula(e.numMatricula());
            if (alunos.existsByNumMatriculaAndIdNot(numMatricula, id)) {
                throw new RegraDeNegocioException("NUM_MATRICULA_EM_USO", "Já existe um aluno com este nº de matrícula.");
            }
            aluno.alterarNumMatricula(numMatricula);
        }
        if (e.novaSenha() != null && !e.novaSenha().isBlank()) {
            usuario.alterarSenhaHash(hash(e.novaSenha()));
        }
        return ContaVisao.de(usuario);
    }

    private static String exigirMatricula(String numMatricula) {
        if (numMatricula == null || numMatricula.isBlank()) {
            throw new RegraDeNegocioException("DADO_INVALIDO", "Informe o nº de matrícula do aluno.");
        }
        return numMatricula.strip();
    }

    private String hash(String senha) {
        // bcrypt só considera os 72 primeiros bytes: recusamos o excesso em vez de truncar em silêncio
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new RegraDeNegocioException("SENHA_INVALIDA", "A senha pode ter no máximo 72 bytes.");
        }
        return codificador.encode(senha);
    }
}
