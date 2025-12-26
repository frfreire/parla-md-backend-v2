package br.gov.md.parla_md_backend.exception;

public class MetricasException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private static final String CODIGO_ERRO = "MET001";

    public MetricasException(String mensagem) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem));
    }

    public MetricasException(String mensagem, Throwable causa) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem), causa);
    }

    public static MetricasException erroCalculo(String detalhes, Throwable causa) {
        return new MetricasException("Erro ao calcular métricas: " + detalhes, causa);
    }

    public static MetricasException periodoInvalido(String detalhes) {
        return new MetricasException("Período inválido: " + detalhes);
    }
}