package br.gov.md.parla_md_backend.domain.enums;

public enum PrioridadeProcesso {

    BAIXA("Baixa", "Prioridade baixa", 1),
    NORMAL("Normal", "Prioridade normal", 2),
    ALTA("Alta", "Prioridade alta", 3),
    URGENTE("Urgente", "Prioridade urgente", 4),
    URGENTISSIMA("Urgentíssima", "Prioridade urgentíssima", 5);

    private final String descricao;
    private final String detalhe;
    private final int nivel;

    PrioridadeProcesso(String descricao, String detalhe, int nivel) {
        this.descricao = descricao;
        this.detalhe = detalhe;
        this.nivel = nivel;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public int getNivel() {
        return nivel;
    }

    public boolean isMaiorQue(PrioridadeProcesso outra) {
        return this.nivel > outra.nivel;
    }

    public boolean isUrgente() {
        return this == URGENTE || this == URGENTISSIMA;
    }

}