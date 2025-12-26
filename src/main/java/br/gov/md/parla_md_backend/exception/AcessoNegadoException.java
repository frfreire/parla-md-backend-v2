package br.gov.md.parla_md_backend.exception;

public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }

    public AcessoNegadoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
