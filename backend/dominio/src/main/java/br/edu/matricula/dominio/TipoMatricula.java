package br.edu.matricula.dominio;

/** Tipo da matrícula, com o limite por semestre: até 4 obrigatórias e 2 optativas (6 no total). */
public enum TipoMatricula {
    OBRIGATORIA(4, "obrigatórias"),
    OPTATIVA(2, "optativas");

    private final int limite;
    private final String plural;

    TipoMatricula(int limite, String plural) {
        this.limite = limite;
        this.plural = plural;
    }

    public int limite() {
        return limite;
    }

    public String plural() {
        return plural;
    }
}
