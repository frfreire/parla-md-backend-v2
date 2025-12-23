package br.gov.md.parla_md_backend.exception;

public class AutenticacaoException extends DominioException {

    private static final long serialVersionUID = 1L;

    public AutenticacaoException(String mensagem) {
        super(mensagem);
    }

    public AutenticacaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}