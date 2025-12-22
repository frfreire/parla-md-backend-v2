package br.gov.md.parla_md_backend.domain.enums;

public enum NivelConfianca {

    MUITO_ALTA("Muito Alta", 0.90, 1.00),
    ALTA("Alta", 0.75, 0.89),
    MEDIA("Média", 0.50, 0.74),
    BAIXA("Baixa", 0.25, 0.49),
    MUITO_BAIXA("Muito Baixa", 0.00, 0.24);

    private final String descricao;
    private final double limiteInferior;
    private final double limiteSuperior;

    NivelConfianca(String descricao, double limiteInferior, double limiteSuperior) {
        this.descricao = descricao;
        this.limiteInferior = limiteInferior;
        this.limiteSuperior = limiteSuperior;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getLimiteInferior() {
        return limiteInferior;
    }

    public double getLimiteSuperior() {
        return limiteSuperior;
    }

    /**
     * Determina nível de confiança a partir de probabilidade
     */
    public static NivelConfianca fromProbabilidade(double probabilidade) {
        for (NivelConfianca nivel : values()) {
            if (probabilidade >= nivel.limiteInferior &&
                    probabilidade <= nivel.limiteSuperior) {
                return nivel;
            }
        }
        return MUITO_BAIXA;
    }
}
