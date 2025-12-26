package br.gov.md.parla_md_backend.domain.enums;

public enum TipoParecer {

    TECNICO("Técnico", "Parecer de natureza técnica"),
    JURIDICO("Jurídico", "Parecer de natureza jurídica"),
    FINANCEIRO("Financeiro", "Parecer sobre aspectos financeiros e orçamentários"),
    ESTRATEGICO("Estratégico", "Parecer sobre aspectos estratégicos"),
    OPERACIONAL("Operacional", "Parecer sobre aspectos operacionais"),
    CONCLUSIVO("Conclusivo", "Parecer conclusivo final");

    private final String descricao;
    private final String detalhe;

    TipoParecer(String descricao, String detalhe) {
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