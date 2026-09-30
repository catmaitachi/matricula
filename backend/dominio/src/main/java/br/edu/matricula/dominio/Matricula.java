package br.edu.matricula.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "turma_id"}))
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Aluno aluno;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Turma turma;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TipoMatricula tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 22)
    private EstadoMatricula estado = EstadoMatricula.ATIVA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 22)
    private StatusCobranca cobranca = StatusCobranca.PENDENTE;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm = Instant.now();

    protected Matricula() {
    }

    Matricula(Aluno aluno, Turma turma, TipoMatricula tipo) {
        this.aluno = aluno;
        this.turma = turma;
        this.tipo = tipo;
    }

    public boolean ativa() {
        return estado == EstadoMatricula.ATIVA;
    }

    public void cobrancaEnviada() {
        if (cobranca == StatusCobranca.PENDENTE) {
            cobranca = StatusCobranca.ENVIADA;
        }
    }

    public void cobrancaCancelada() {
        cobranca = StatusCobranca.CANCELADA;
    }

    void cancelarPorFaltaDeQuorum() {
        estado = EstadoMatricula.CANCELADA_SEM_QUORUM;
        // se a cobrança nem chegou a sair, não há o que cancelar no sistema externo
        cobranca = cobranca == StatusCobranca.PENDENTE ? StatusCobranca.CANCELADA : StatusCobranca.CANCELAMENTO_PENDENTE;
    }

    /** Chave estável usada como identificador idempotente no sistema de pagamento. */
    public static String chaveDeCobranca(Long matriculaId) {
        return "matricula-" + matriculaId;
    }

    public String chaveDeCobranca() {
        return chaveDeCobranca(id);
    }

    public Long id() {
        return id;
    }

    public Aluno aluno() {
        return aluno;
    }

    public Turma turma() {
        return turma;
    }

    public TipoMatricula tipo() {
        return tipo;
    }

    public EstadoMatricula estado() {
        return estado;
    }

    public StatusCobranca cobranca() {
        return cobranca;
    }

    public Instant criadaEm() {
        return criadaEm;
    }
}
