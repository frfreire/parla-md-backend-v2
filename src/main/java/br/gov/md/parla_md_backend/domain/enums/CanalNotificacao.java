package br.gov.md.parla_md_backend.domain.enums;

public enum CanalNotificacao {

    EMAIL("E-mail", "Notificação por e-mail"),
    PUSH("Push", "Notificação push no dispositivo móvel"),
    SISTEMA("Sistema", "Notificação dentro do sistema"),
    WHATSAPP("WhatsApp", "Notificação via WhatsApp");

    private final String nome;
    private final String descricao;

    CanalNotificacao(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}
