package br.gov.md.parla_md_backend.domain.enums;

public enum TipoProposicao {
    PL("Projeto de Lei"),
    PLP("Projeto de Lei Complementar"),
    PEC("Proposta de Emenda à Constituição"),
    PDC("Projeto de Decreto Legislativo"),
    PRC("Projeto de Resolução"),
    MPV("Medida Provisória"),
    PLV("Projeto de Lei de Conversão");

    private final String descricao;

    TipoProposicao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}