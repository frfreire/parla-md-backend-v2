package br.gov.md.parla_md_backend.domain.enums;

public enum StatusPosicionamento {

    PENDENTE("Pendente", "Aguardando manifestação"),
    RECEBIDO("Recebido", "Posicionamento recebido"),
    EM_ANALISE("Em Análise", "Em análise interna"),
    CONSOLIDADO("Consolidado", "Posicionamento consolidado"),
    ENVIADO("Enviado", "Posicionamento enviado ao solicitante"),
    CANCELADO("Cancelado", "Posicionamento cancelado");

    private final String descricao;
    private final String detalhe;

    StatusPosicionamento(String descricao, String detalhe) {
        this.descricao = descricao;
        this.detalhe = detalhe;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public boolean isPendente() {
        return this == PENDENTE;
    }

    public boolean isFinalizado() {
        return this == CONSOLIDADO || this == ENVIADO || this == CANCELADO;
    }
}