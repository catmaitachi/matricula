package br.edu.matricula.dominio;

/** Violação de uma regra do negócio. O {@code codigo} é estável (usado pela API e pelos testes); a mensagem é para o usuário. */
public class RegraDeNegocioException extends RuntimeException {

    private final String codigo;

    public RegraDeNegocioException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
