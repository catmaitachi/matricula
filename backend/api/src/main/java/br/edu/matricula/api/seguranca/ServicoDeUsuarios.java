package br.edu.matricula.api.seguranca;

import br.edu.matricula.api.repositorio.UsuarioRepositorio;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
class ServicoDeUsuarios implements UserDetailsService {

    private final UsuarioRepositorio usuarios;
    private final TentativasDeLogin tentativas;

    ServicoDeUsuarios(UsuarioRepositorio usuarios, TentativasDeLogin tentativas) {
        this.usuarios = usuarios;
        this.tentativas = tentativas;
    }

    @Override
    public UserDetails loadUserByUsername(String numPessoa) {
        if (tentativas.bloqueado(numPessoa)) {
            return UsuarioLogado.bloqueado(numPessoa);
        }
        return usuarios.findByNumPessoa(numPessoa.strip())
                .map(UsuarioLogado::de)
                .orElseThrow(() -> new UsernameNotFoundException("credenciais inválidas"));
    }
}
