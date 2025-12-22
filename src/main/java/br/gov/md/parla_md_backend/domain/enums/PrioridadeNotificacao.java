package br.gov.md.parla_md_backend.domain.enums;

public enum PrioridadeNotificacao {

    BAIXA("Baixa", 1),
    NORMAL("Normal", 2),
    ALTA("Alta", 3),
    URGENTE("Urgente", 4);

    private final String descricao;
    private final int nivel;

    PrioridadeNotificacao(String descricao, int nivel) {
        this.descricao = descricao;
        this.nivel = nivel;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getNivel() {
        return nivel;
    }

    public boolean isMaiorQue(PrioridadeNotificacao outra) {
        return this.nivel > outra.nivel;
    }
}