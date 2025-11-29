package br.gov.md.parla_md_backend.exception;

/**
 * Exceção base para problemas de autenticação.
 *
 * @author fabricio.freire
 * @since 1.0
 */
public class AutenticacaoException extends DominioException {

    /**
     * É uma boa prática adicionar um serialVersionUID em classes de exceção,
     * mesmo que a superclasse já o tenha, para garantir compatibilidade
     * durante a serialização/desserialização desta classe específica.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construtor para AutenticacaoException com uma mensagem.
     * @param mensagem A mensagem de detalhe da exceção.
     */
    public AutenticacaoException(String mensagem) {
        super(mensagem);
    }

    /**
     * Construtor para AutenticacaoException com uma mensagem e uma causa.
     * Este construtor permite encadear a exceção original que levou a esta.
     * @param mensagem A mensagem de detalhe da exceção.
     * @param causa A causa raiz (que é salva para recuperação posterior pelo método getCause()).
     */
    public AutenticacaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}