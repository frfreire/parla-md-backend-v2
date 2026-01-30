package br.gov.md.parla_md_backend.exception;

public class TokenExpiradoException extends RuntimeException {
    public TokenExpiradoException(String mensagem) {
        super(mensagem);
    }
}
