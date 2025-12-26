package br.gov.md.parla_md_backend.domain.enums;

public enum TendenciaVoto {

    FAVORAVEL("Favorável", "Tende a votar a favor"),
    CONTRARIO("Contrário", "Tende a votar contra"),
    ABSTENCAO("Abstenção", "Tende a se abster"),
    INCERTO("Incerto", "Tendência incerta ou mista");

    private final String descricao;
    private final String detalhe;

    TendenciaVoto(String descricao, String detalhe) {
        this.descricao = descricao;
        this.detalhe = detalhe;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDetalhe() {
        return detalhe;
    }

}