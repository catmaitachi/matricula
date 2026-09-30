package br.edu.matricula.dominio;

/** ABERTA enquanto há inscrições; ao encerrar vira CONFIRMADA (tem quórum) ou CANCELADA (não tem). */
public enum EstadoTurma { ABERTA, CONFIRMADA, CANCELADA }
