package br.gov.md.parla_md_backend.exception;

public class SumarizacaoException extends IAException {

    private static final long serialVersionUID = 1L;
    private static final String CODIGO_ERRO = "SUM001";

    public SumarizacaoException(String mensagem) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem));
    }

    public SumarizacaoException(String mensagem, Throwable causa) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem), causa);
    }

    public static SumarizacaoException textoVazio() {
        return new SumarizacaoException("Texto para sumarização não pode ser vazio");
    }

    public static SumarizacaoException textoMuitoCurto(int minimoCaracteres) {
        return new SumarizacaoException(
                "Texto muito curto para sumarização. Mínimo: " + minimoCaracteres + " caracteres"
        );
    }

    public static SumarizacaoException erroProcessamento(String detalhes, Throwable causa) {
        return new SumarizacaoException("Erro ao processar sumarização: " + detalhes, causa);
    }
}
