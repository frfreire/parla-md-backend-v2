package br.gov.md.parla_md_backend.domain.enums;

public enum TipoTramitacao {

    ENCAMINHAMENTO("Encaminhamento", "Encaminhamento inicial do processo"),
    SOLICITACAO_PARECER("Solicitação de Parecer", "Solicitação de parecer técnico"),
    RETORNO_PARECER("Retorno de Parecer", "Retorno com parecer emitido"),
    SOLICITACAO_POSICIONAMENTO("Solicitação de Posicionamento", "Solicitação de posicionamento institucional"),
    RETORNO_POSICIONAMENTO("Retorno de Posicionamento", "Retorno com posicionamento"),
    REDISTRIBUICAO("Redistribuição", "Redistribuição para outro setor/órgão"),
    DEVOLUCAO("Devolução", "Devolução ao remetente"),
    ARQUIVAMENTO("Arquivamento", "Encaminhamento para arquivamento"),
    INFORMACAO("Informação", "Envio de informação complementar");

    private final String descricao;
    private final String detalhe;

    TipoTramitacao(String descricao, String detalhe) {
        this.descricao = descricao;
        this.detalhe = detalhe;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public boolean requerResposta() {
        return this == SOLICITACAO_PARECER || this == SOLICITACAO_POSICIONAMENTO;
    }

    public boolean isRetorno() {
        return this == RETORNO_PARECER || this == RETORNO_POSICIONAMENTO;
    }

    public boolean isEncaminhamento() {
        return this == ENCAMINHAMENTO;
    }
}
