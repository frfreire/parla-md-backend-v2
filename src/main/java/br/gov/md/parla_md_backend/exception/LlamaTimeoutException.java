package br.gov.md.parla_md_backend.exception;

public class LlamaTimeoutException extends LlamaIndisponivelException {

    private static final long serialVersionUID = 1L;
    private static final String CODIGO_ERRO = "IA003";

    private final long tempoEsperaMs;

    public LlamaTimeoutException(String mensagem, long tempoEsperaMs) {
        super(String.format("[%s] %s (Tempo: %dms)", CODIGO_ERRO, mensagem, tempoEsperaMs));
        this.tempoEsperaMs = tempoEsperaMs;
    }

    public static LlamaTimeoutException timeoutExcedido(long tempoEsperaMs) {
        return new LlamaTimeoutException(
                "Tempo limite excedido ao aguardar resposta do Llama",
                tempoEsperaMs
        );
    }

    public long getTempoEsperaMs() {
        return tempoEsperaMs;
    }
}