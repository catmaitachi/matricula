package br.edu.matricula.api;

import br.edu.matricula.api.repositorio.UsuarioRepositorio;
import br.edu.matricula.dominio.Secretaria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sem nenhuma conta ninguém consegue entrar, e não existe senha padrão. Se a variável {@code MATRICULA_BOOTSTRAP_SENHA}
 * estiver definida e a conta ainda não existir, cria a primeira secretaria com essa senha; a partir dela, as demais
 * contas são criadas pela própria tela. Sem a variável, não faz nada.
 */
@Component
class PrimeiraSecretaria implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PrimeiraSecretaria.class);
    static final int TAMANHO_MINIMO = 12;

    private final UsuarioRepositorio usuarios;
    private final PasswordEncoder codificador;
    private final String numPessoa;
    private final String nome;
    private final String senha;

    PrimeiraSecretaria(UsuarioRepositorio usuarios, PasswordEncoder codificador,
                       @Value("${matricula.bootstrap.num-pessoa:SEC001}") String numPessoa,
                       @Value("${matricula.bootstrap.nome:Secretaria}") String nome,
                       @Value("${matricula.bootstrap.senha:}") String senha) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.numPessoa = numPessoa;
        this.nome = nome;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (senha.isBlank() || usuarios.existsByNumPessoa(numPessoa)) {
            return;
        }
        if (senha.length() < TAMANHO_MINIMO) {
            throw new IllegalStateException("MATRICULA_BOOTSTRAP_SENHA precisa ter pelo menos " + TAMANHO_MINIMO + " caracteres.");
        }
        usuarios.save(new Secretaria(numPessoa, nome, codificador.encode(senha)));
        log.info("Primeira secretaria criada: {}", numPessoa);
    }
}
