package br.gov.md.parla_md_backend.exception;

public class LlamaIndisponivelException extends IAException {

    private static final long serialVersionUID = 1L;
    private static final String CODIGO_ERRO = "IA002";

    public LlamaIndisponivelException(String mensagem) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem));
    }

    public LlamaIndisponivelException(String mensagem, Throwable causa) {
        super(String.format("[%s] %s", CODIGO_ERRO, mensagem), causa);
    }

    public static LlamaIndisponivelException servicoOffline() {
        return new LlamaIndisponivelException(
                "Serviço Llama está indisponível. Tente novamente em instantes."
        );
    }

    public static LlamaIndisponivelException erroConexao(Throwable causa) {
        return new LlamaIndisponivelException(
                "Erro ao conectar com Ollama",
                causa
        );
    }
}
