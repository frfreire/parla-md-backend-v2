package br.gov.md.parla_md_backend.domain.enums;

public enum Casa {
    CAMARA("Câmara dos Deputados"),
    SENADO("Senado Federal");

    private final String nome;

    Casa(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}