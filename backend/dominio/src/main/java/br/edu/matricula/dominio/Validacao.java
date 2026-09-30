package br.edu.matricula.dominio;

/** Verificações de invariantes compartilhadas pelas entidades. */
final class Validacao {

    private Validacao() {
    }

    static String texto(String valor, int max, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraDeNegocioException("DADO_INVALIDO", campo + " é obrigatório.");
        }
        var limpo = valor.strip();
        if (limpo.length() > max) {
            throw new RegraDeNegocioException("DADO_INVALIDO", campo + " deve ter no máximo " + max + " caracteres.");
        }
        return limpo;
    }
}
