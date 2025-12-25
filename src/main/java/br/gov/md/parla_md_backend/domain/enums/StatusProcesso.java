package br.gov.md.parla_md_backend.domain.enums;

public enum StatusProcesso {

    CRIADO("Processo criado"),
    INICIADO("Processo que já possui documentos associados"),
    EM_ANALISE_INTERNA("Em análise interna no MD"),
    AGUARDANDO_PARECER_SETOR("Aguardando parecer de setor interno"),
    PARECERES_INTERNOS_RECEBIDOS("Pareceres internos recebidos"),
    CONSOLIDACAO_INTERNA("Em consolidação de pareceres internos"),
    AGUARDANDO_POSICIONAMENTO_EXTERNO("Aguardando posicionamento de órgão externo"),
    POSICIONAMENTOS_EXTERNOS_RECEBIDOS("Posicionamentos externos recebidos"),
    EM_ANALISE_FINAL("Em análise final pelo gabinete"),
    AGUARDANDO_DECISAO("Aguardando decisão da autoridade"),
    FINALIZADO("Processo finalizado com posição definida"),
    ARQUIVADO("Processo arquivado"),
    SUSPENSO("Processo temporariamente suspenso"),
    EM_ANDAMENTO("Processo em tramitação");

    private final String descricao;

    StatusProcesso(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}