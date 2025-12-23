package br.gov.md.parla_md_backend.exception;

public class DominioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DominioException(String mensagem) {
        super(mensagem);
    }

    public DominioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}