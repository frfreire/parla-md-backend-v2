package br.gov.md.parla_md_backend.domain.enums;

public enum TipoPosicionamento {

    FAVORAVEL("Favorável", "Posicionamento favorável"),
    CONTRARIO("Contrário", "Posicionamento contrário"),
    FAVORAVEL_COM_RESSALVAS("Favorável com Ressalvas", "Favorável com observações"),
    SEM_OBJECAO("Sem Objeção", "Não há objeções"),
    ABSTENÇÃO("Abstenção", "Órgão se abstém de opinar"),
    AGUARDAR_ANALISE("Aguardar Análise", "Aguardar análise mais aprofundada");

    private final String descricao;
    private final String detalhe;

    TipoPosicionamento(String descricao, String detalhe) {
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
        return this == FAVORAVEL || this == FAVORAVEL_COM_RESSALVAS || this == SEM_OBJECAO;
    }

}
