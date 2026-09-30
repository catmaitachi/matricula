package br.edu.matricula.api.servico;

/** O recurso não existe ou não pertence a quem pediu (a resposta é a mesma, para não revelar o que existe). */
public class NaoEncontradoException extends RuntimeException {

    private final String codigo;

    public NaoEncontradoException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public NaoEncontradoException(String mensagem) {
        this("NAO_ENCONTRADO", mensagem);
    }

    public String codigo() {
        return codigo;
    }
}
