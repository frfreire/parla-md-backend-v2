package br.gov.md.parla_md_backend.domain.enums;

public enum TipoRestricao {

    LEITURA("Leitura", "Permite apenas visualizar"),

    EDICAO("Edição", "Permite visualizar e editar"),

    TOTAL("Total", "Acesso total incluindo gestão de permissões");

    private final String descricao;
    private final String detalhe;

    TipoRestricao(String descricao, String detalhe) {
        this.descricao = descricao;
        this.detalhe = detalhe;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public boolean permiteEdicao() {
        return this == EDICAO || this == TOTAL;
    }

    public boolean permiteGestaoPermissoes() {
        return this == TOTAL;
    }
}
