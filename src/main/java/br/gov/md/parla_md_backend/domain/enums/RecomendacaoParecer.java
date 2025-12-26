package br.gov.md.parla_md_backend.domain.enums;

public enum RecomendacaoParecer {

    FAVORAVEL("Favorável", "Recomenda aprovação integral"),
    FAVORAVEL_COM_EMENDAS("Favorável com Emendas", "Recomenda aprovação com modificações"),
    CONTRARIO("Contrário", "Recomenda rejeição"),
    ABSTENÇÃO("Abstenção", "Não opina sobre o mérito"),
    INFORMATIVO("Informativo", "Apenas informa, sem emitir opinião"),
    AGUARDAR_MANIFESTACAO("Aguardar Manifestação", "Recomenda aguardar outras manifestações");

    private final String descricao;
    private final String detalhe;

    RecomendacaoParecer(String descricao, String detalhe) {
        this.descricao = descricao;
        this.detalhe = detalhe;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public boolean isFavoravel() {
        return this == FAVORAVEL || this == FAVORAVEL_COM_EMENDAS;
    }

    public boolean isContrario() {
        return this == CONTRARIO;
    }

}