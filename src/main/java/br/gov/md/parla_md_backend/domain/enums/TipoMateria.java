package br.gov.md.parla_md_backend.domain.enums;

public enum TipoMateria {

    PLS("Projeto de Lei do Senado"),
    PLC("Projeto de Lei da Câmara"),
    PEC("Proposta de Emenda à Constituição"),
    PDS("Projeto de Decreto Legislativo do Senado"),
    PDC("Projeto de Decreto Legislativo da Câmara"),
    PRS("Projeto de Resolução do Senado"),
    MSF("Mensagem do Senado Federal"),
    MPV("Medida Provisória"),
    PLV("Projeto de Lei de Conversão");

    private final String descricao;

    TipoMateria(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getSigla() {
        return this.name();
    }
}