package br.gov.md.parla_md_backend.exception;

public class UsuarioBloqueadoException extends RuntimeException {
    public UsuarioBloqueadoException(String mensagem) {
        super(mensagem);
    }
}
