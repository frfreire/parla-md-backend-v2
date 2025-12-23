package br.gov.md.parla_md_backend.exception;

public class TramitacaoInvalidaException extends RuntimeException {

    public TramitacaoInvalidaException(String mensagem) {
        super(mensagem);
    }

    public TramitacaoInvalidaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}