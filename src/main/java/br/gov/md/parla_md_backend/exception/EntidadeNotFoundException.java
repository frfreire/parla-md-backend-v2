package br.gov.md.parla_md_backend.exception;

public class EntidadeNotFoundException extends DominioException {

    public EntidadeNotFoundException(String entidade, String id) {
        super(String.format("%s não encontrado(a) com ID: %s", entidade, id));
    }
}
