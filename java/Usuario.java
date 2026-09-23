

public abstract class Usuario {
    private String nome;
    private String numPessoa;
    private String senha;

    public Usuario() {
    }

    public Usuario(String nome, String numPessoa, String senha) {
        this.nome = nome;
        this.numPessoa = numPessoa;
        this.senha = senha;
    }

    public void realizarLogin() {
        System.out.println("[Stub] Usuario.realizarLogin(): Autenticando usuario " + nome + " (numPessoa: " + numPessoa + ")");
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNumPessoa() {
        return numPessoa;
    }

    public void setNumPessoa(String numPessoa) {
        this.numPessoa = numPessoa;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "nome='" + nome + '\'' +
                ", numPessoa='" + numPessoa + '\'' +
                '}';
    }
}
