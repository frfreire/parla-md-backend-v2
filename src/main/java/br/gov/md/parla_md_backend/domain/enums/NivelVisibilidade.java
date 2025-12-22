package br.gov.md.parla_md_backend.domain.enums;

/**
 * Níveis de visibilidade para documentos (pareceres e despachos)
 */
public enum NivelVisibilidade {

    /**
     * Documento visível para todos os usuários autenticados do sistema
     */
    PUBLICO("Público", "Visível para todos os usuários do sistema"),

    /**
     * Documento visível apenas para usuários de setores específicos
     */
    RESTRITO_SETOR("Restrito por Setor", "Visível apenas para setores autorizados"),

    /**
     * Documento visível apenas para usuários específicos
     */
    RESTRITO_INDIVIDUAL("Restrito Individual", "Visível apenas para usuários específicos"),

    /**
     * Documento visível apenas para o autor
     */
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
