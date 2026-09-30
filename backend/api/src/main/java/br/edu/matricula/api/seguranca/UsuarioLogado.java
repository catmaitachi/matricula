package br.edu.matricula.api.seguranca;

import br.edu.matricula.dominio.Papel;
import br.edu.matricula.dominio.Usuario;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Quem está na sessão. O hash da senha é apagado assim que a autenticação termina. */
public final class UsuarioLogado implements UserDetails, CredentialsContainer, Serializable {

    private final Long id;
    private final String numPessoa;
    private final Papel papel;
    private final boolean ativo;
    private final boolean bloqueado;
    private String senhaHash;

    private UsuarioLogado(Long id, String numPessoa, Papel papel, String senhaHash, boolean ativo, boolean bloqueado) {
        this.id = id;
        this.numPessoa = numPessoa;
        this.papel = papel;
        this.senhaHash = senhaHash;
        this.ativo = ativo;
        this.bloqueado = bloqueado;
    }

    static UsuarioLogado de(Usuario usuario) {
        return new UsuarioLogado(usuario.id(), usuario.numPessoa(), usuario.papel(), usuario.senhaHash(), usuario.ativo(), false);
    }

    /** Usado quando há muitas tentativas: existindo ou não a conta, a resposta é a mesma. */
    static UsuarioLogado bloqueado(String numPessoa) {
        return new UsuarioLogado(-1L, numPessoa, Papel.ALUNO, "", true, true);
    }

    public Long id() {
        return id;
    }

    public Papel papel() {
        return papel;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + papel.name()));
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return numPessoa;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !bloqueado;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }

    @Override
    public void eraseCredentials() {
        senhaHash = null;
    }
}
