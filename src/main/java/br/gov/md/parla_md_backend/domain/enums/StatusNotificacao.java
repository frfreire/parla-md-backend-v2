package br.gov.md.parla_md_backend.domain.enums;

public enum StatusNotificacao {

    PENDENTE("Pendente", "Aguardando envio"),
    ENVIADA("Enviada", "Enviada com sucesso"),
    LIDA("Lida", "Visualizada pelo destinatário"),
    ERRO("Erro", "Erro no envio"),
    CANCELADA("Cancelada", "Cancelada antes do envio");

    private final String descricao;
    private final String detalhe;

    StatusNotificacao(String descricao, String detalhe) {
        this.descricao = descricao;
        this.detalhe = detalhe;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public boolean isFinal() {
        return this == LIDA || this == ERRO || this == CANCELADA;
    }

}
