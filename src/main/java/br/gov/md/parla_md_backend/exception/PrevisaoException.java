package br.gov.md.parla_md_backend.exception;

public class PrevisaoException extends IAException {

    private static final long serialVersionUID = 1L;
    private static final String CODIGO_ERRO = "ML002";

    public PrevisaoException(String mensagem) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem));
    }

    public PrevisaoException(String mensagem, Throwable causa) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem), causa);
    }

    public static PrevisaoException erroCalculo(String detalhes, Throwable causa) {
        return new PrevisaoException("Erro ao calcular previsão: " + detalhes, causa);
    }

    public static PrevisaoException dadosInsuficientes() {
        return new PrevisaoException("Dados insuficientes para realizar previsão");
    }
}