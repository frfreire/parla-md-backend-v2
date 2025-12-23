package br.gov.md.parla_md_backend.domain.enums;

public enum AlinhamentoPolitico {

    GOVERNO("Governo", "Alinhado com o governo"),
    OPOSICAO("Oposição", "Alinhado com a oposição"),
    INDEPENDENTE("Independente", "Votação independente"),
    MISTO("Misto", "Alinhamento misto/variável");

    private final String descricao;
    private final String detalhe;

    AlinhamentoPolitico(String descricao, String detalhe) {
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
