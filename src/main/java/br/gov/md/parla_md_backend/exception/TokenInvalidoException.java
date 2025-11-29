package br.gov.md.parla_md_backend.exception;

/**
 * Exceção para tokens JWT inválidos ou expirados.
 *
 * @author fabricio.freire
 * @since 1.0
 */
public class TokenInvalidoException extends AutenticacaoException {

    public TokenInvalidoException(String mensagem) {
        super("Token inválido: " + mensagem);
    }

    public TokenInvalidoException(String mensagem, Throwable causa) {
        super("Token inválido: " + mensagem, causa);
    }
}