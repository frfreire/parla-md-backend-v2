package br.gov.md.parla_md_backend.exception;

public class AnaliseParlamentarException extends IAException {

    private static final long serialVersionUID = 1L;
    private static final String CODIGO_ERRO = "PAR001";

    public AnaliseParlamentarException(String mensagem) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem));
    }

    public AnaliseParlamentarException(String mensagem, Throwable causa) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem), causa);
    }

    public static AnaliseParlamentarException parlamentarNaoEncontrado(String parlamentarId) {
        return new AnaliseParlamentarException(
                "Parlamentar não encontrado: " + parlamentarId);
    }

    public static AnaliseParlamentarException dadosInsuficientes(String parlamentarId, String tema) {
        return new AnaliseParlamentarException(
                String.format("Dados insuficientes para análise: parlamentar=%s, tema=%s",
                        parlamentarId, tema));
    }

    public static AnaliseParlamentarException erroProcessamento(String detalhes, Throwable causa) {
        return new AnaliseParlamentarException(
                "Erro ao processar análise parlamentar: " + detalhes, causa);
    }
}