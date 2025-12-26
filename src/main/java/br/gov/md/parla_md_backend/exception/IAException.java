package br.gov.md.parla_md_backend.exception;

public class IAException extends DominioException {

    private static final long serialVersionUID = 1L;
    private static final String CODIGO_ERRO = "IA001";

    public IAException(String mensagem) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem));
    }

    public IAException(String mensagem, Throwable causa) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem), causa);
    }

    public static IAException respostaInvalida(String detalhes) {
        return new IAException("Resposta do LLM em formato inválido: " + detalhes);
    }

    public static IAException respostaIncompleta() {
        return new IAException("Resposta incompleta recebida do LLM");
    }

    public static IAException processingError(String detalhes, Throwable causa) {
        return new IAException("Erro ao processar requisição de IA: " + detalhes, causa);
    }
}