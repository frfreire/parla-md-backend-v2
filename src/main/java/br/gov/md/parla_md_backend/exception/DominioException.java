package br.gov.md.parla_md_backend.exception;

public class DominioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DominioException(String mensagem) {
        super(mensagem);
    }

    /**
     * Construtor que aceita uma mensagem e a causa da exceção.
     * @param mensagem A mensagem de detalhe.
     * @param causa A causa raiz (que é salva para recuperação posterior por meio do método getCause()).
     */
    public DominioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}