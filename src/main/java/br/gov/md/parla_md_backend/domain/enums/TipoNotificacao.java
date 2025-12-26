package br.gov.md.parla_md_backend.domain.enums;

public enum TipoNotificacao {

    // Tramitação
    TRAMITACAO_RECEBIDA("Tramitação Recebida", "Novo processo tramitado para seu setor"),
    TRAMITACAO_PRAZO_PROXIMO("Prazo Próximo", "Prazo de tramitação se aproximando"),
    TRAMITACAO_PRAZO_VENCIDO("Prazo Vencido", "Prazo de tramitação vencido"),

    // Parecer
    PARECER_SOLICITADO("Parecer Solicitado", "Solicitação de parecer técnico"),
    PARECER_EMITIDO("Parecer Emitido", "Novo parecer foi emitido"),
    PARECER_APROVADO("Parecer Aprovado", "Parecer foi aprovado"),
    PARECER_REPROVADO("Parecer Reprovado", "Parecer foi reprovado"),
    PARECER_PRAZO_PROXIMO("Prazo Parecer Próximo", "Prazo para emissão de parecer se aproximando"),
    PARECER_PRAZO_VENCIDO("Prazo Parecer Vencido", "Prazo para emissão de parecer vencido"),

    // Posicionamento
    POSICIONAMENTO_SOLICITADO("Posicionamento Solicitado", "Solicitação de posicionamento institucional"),
    POSICIONAMENTO_RECEBIDO("Posicionamento Recebido", "Novo posicionamento foi recebido"),
    POSICIONAMENTO_PRAZO_PROXIMO("Prazo Posicionamento Próximo", "Prazo para posicionamento se aproximando"),
    POSICIONAMENTO_PRAZO_VENCIDO("Prazo Posicionamento Vencido", "Prazo para posicionamento vencido"),

    // Processo
    PROCESSO_CRIADO("Processo Criado", "Novo processo legislativo criado"),
    PROCESSO_ATUALIZADO("Processo Atualizado", "Processo legislativo atualizado"),
    PROCESSO_FINALIZADO("Processo Finalizado", "Processo legislativo finalizado"),
    PROCESSO_ATRIBUIDO("Processo Atribuído", "Processo atribuído a você"),

    // Visibilidade
    PERMISSAO_CONCEDIDA("Permissão Concedida", "Você recebeu acesso a um documento"),
    PERMISSAO_REVOGADA("Permissão Revogada", "Seu acesso a um documento foi revogado"),

    // Sistema
    ATUALIZACAO_SISTEMA("Atualização do Sistema", "Atualização ou manutenção do sistema"),
    MENSAGEM_ADMINISTRACAO("Mensagem da Administração", "Comunicado da administração do sistema");

    private final String titulo;
    private final String descricao;

    TipoNotificacao(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isPrazo() {
        return name().contains("PRAZO");
    }

    public boolean isUrgente() {
        return name().contains("VENCIDO") || this == TRAMITACAO_RECEBIDA;
    }
}