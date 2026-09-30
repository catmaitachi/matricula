package br.edu.matricula.api;

import br.edu.matricula.api.repositorio.AlunoRepositorio;
import br.edu.matricula.api.repositorio.CurriculoRepositorio;
import br.edu.matricula.api.repositorio.CursoRepositorio;
import br.edu.matricula.api.repositorio.DisciplinaRepositorio;
import br.edu.matricula.api.repositorio.MatriculaRepositorio;
import br.edu.matricula.api.repositorio.ProfessorRepositorio;
import br.edu.matricula.api.repositorio.UsuarioRepositorio;
import br.edu.matricula.dominio.Aluno;
import br.edu.matricula.dominio.Curriculo;
import br.edu.matricula.dominio.Curso;
import br.edu.matricula.dominio.Disciplina;
import br.edu.matricula.dominio.Professor;
import br.edu.matricula.dominio.Secretaria;
import br.edu.matricula.dominio.TipoMatricula;
import br.edu.matricula.dominio.Turno;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contas e dados de exemplo, só com {@code matricula.seed.habilitado=true} (perfil dev). As senhas são
 * conhecidas e públicas: nunca ligar isto num ambiente que não seja de desenvolvimento.
 */
@Component
@ConditionalOnProperty(name = "matricula.seed.habilitado", havingValue = "true")
class DadosIniciais implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);

    private final UsuarioRepositorio usuarios;
    private final AlunoRepositorio alunos;
    private final ProfessorRepositorio professores;
    private final CursoRepositorio cursos;
    private final DisciplinaRepositorio disciplinas;
    private final CurriculoRepositorio curriculos;
    private final MatriculaRepositorio matriculas;
    private final PasswordEncoder codificador;

    DadosIniciais(UsuarioRepositorio usuarios, AlunoRepositorio alunos, ProfessorRepositorio professores, CursoRepositorio cursos,
                  DisciplinaRepositorio disciplinas, CurriculoRepositorio curriculos, MatriculaRepositorio matriculas,
                  PasswordEncoder codificador) {
        this.usuarios = usuarios;
        this.alunos = alunos;
        this.professores = professores;
        this.cursos = cursos;
        this.disciplinas = disciplinas;
        this.curriculos = curriculos;
        this.matriculas = matriculas;
        this.codificador = codificador;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.count() > 0) {
            return;
        }
        log.warn("Carga inicial de DESENVOLVIMENTO: contas de exemplo com senhas públicas.");

        usuarios.save(new Secretaria("SEC001", "Maria Silva", codificador.encode("senhaSec123")));
        var carlos = professores.save(new Professor("PROF100", "Dr. Carlos Eduardo", codificador.encode("senhaProf456")));
        var helena = professores.save(new Professor("PROF101", "Profa. Helena Duarte", codificador.encode("senhaProf789")));
        var joao = alunos.save(new Aluno("ALU999", "João Pedro", codificador.encode("senhaAlu789"), "20261001"));
        var ana = alunos.save(new Aluno("ALU001", "Ana Beatriz", codificador.encode("senhaAlu123"), "20261002"));
        var bruno = alunos.save(new Aluno("ALU002", "Bruno Costa", codificador.encode("senhaAlu123"), "20261003"));
        var carla = alunos.save(new Aluno("ALU003", "Carla Nunes", codificador.encode("senhaAlu123"), "20261004"));

        var es = cursos.save(new Curso("Engenharia de Software", 240));
        var cc = cursos.save(new Curso("Ciência da Computação", 240));
        var ambos = Set.of(es, cc);
        var engSoft = disciplinas.save(new Disciplina("Engenharia de Software", 3, 60, Set.of(es)));
        var lab = disciplinas.save(new Disciplina("Laboratório de Desenvolvimento de Software", 3, 60, Set.of(es)));
        var calculo = disciplinas.save(new Disciplina("Cálculo II", 3, 60, ambos));
        var redes = disciplinas.save(new Disciplina("Redes de Computadores", 3, 60, Set.of(cc)));
        var bd = disciplinas.save(new Disciplina("Banco de Dados", 3, 60, ambos));
        var ia = disciplinas.save(new Disciplina("Inteligência Artificial", 3, 60, Set.of(cc)));
        var seminario = disciplinas.save(new Disciplina("Seminário de Pesquisa", 1, 2, ambos));

        var curriculo = new Curriculo("2026/2");
        var turmaEs = curriculo.adicionarTurma(engSoft, carlos, Turno.MANHA);
        curriculo.adicionarTurma(lab, carlos, Turno.NOITE);
        curriculo.adicionarTurma(calculo, helena, Turno.TARDE);
        curriculo.adicionarTurma(redes, helena, Turno.NOITE);
        var redesB = curriculo.adicionarTurma(redes, carlos, Turno.TARDE);
        curriculo.adicionarTurma(bd, carlos, Turno.MANHA);
        curriculo.adicionarTurma(ia, helena, Turno.NOITE);
        var turmaSeminario = curriculo.adicionarTurma(seminario, helena, Turno.TARDE);
        curriculo.abrir();
        curriculos.saveAndFlush(curriculo);

        // situações para ver na tela: uma turma com quórum, uma lotada (2/2) e uma abaixo do mínimo (2/3)
        matricular(ana, turmaEs, TipoMatricula.OBRIGATORIA);
        matricular(bruno, turmaEs, TipoMatricula.OBRIGATORIA);
        matricular(carla, turmaEs, TipoMatricula.OBRIGATORIA);
        matricular(ana, turmaSeminario, TipoMatricula.OPTATIVA);
        matricular(bruno, turmaSeminario, TipoMatricula.OPTATIVA);
        matricular(ana, redesB, TipoMatricula.OPTATIVA);
        matricular(bruno, redesB, TipoMatricula.OPTATIVA);
        log.info("Dados de exemplo criados. Aluno de teste: {} (semestre {} aberto).", joao.numPessoa(), curriculo.semestre());
    }

    private void matricular(Aluno aluno, br.edu.matricula.dominio.Turma turma, TipoMatricula tipo) {
        var matricula = matriculas.save(aluno.matricular(turma, tipo));
        matricula.cobrancaEnviada();
    }
}
