package br.gov.md.parla_md_backend.domain.enums;

public enum NivelVisibilidade {

    PUBLICO("Público", "Visível para todos os usuários do sistema"),

    RESTRITO_SETOR("Restrito por Setor", "Visível apenas para setores autorizados"),

    RESTRITO_INDIVIDUAL("Restrito Individual", "Visível apenas para usuários específicos"),

    PRIVADO("Privado", "Visível apenas para o autor");

    private final String descricao;
    private final String detalhe;

    NivelVisibilidade(String descricao, String detalhe) {
        this.descricao = descricao;
        this.detalhe = detalhe;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public boolean isPublico() {
        return this == PUBLICO;
    }

    public boolean isRestrito() {
        return this == RESTRITO_SETOR || this == RESTRITO_INDIVIDUAL;
    }

    public boolean isPrivado() {
        return this == PRIVADO;
    }
}
