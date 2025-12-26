package br.gov.md.parla_md_backend.exception;

public class TokenInvalidoException extends AutenticacaoException {

    public TokenInvalidoException(String mensagem) {
        super("Token inválido: " + mensagem);
    }

    public TokenInvalidoException(String mensagem, Throwable causa) {
        super("Token inválido: " + mensagem, causa);
    }
}