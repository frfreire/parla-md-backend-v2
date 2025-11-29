package br.gov.md.parla_md_backend.exception;

/**
 * Exceção para casos de permissões insuficientes.
 *
 * @author fabricio.freire
 * @since 1.0
 */
public class PermissaoInsuficienteException extends AutenticacaoException {

    public PermissaoInsuficienteException(String recurso) {
        super("Permissões insuficientes para acessar: " + recurso);
    }

    public PermissaoInsuficienteException(String recurso, String permissaoNecessaria) {
        super(String.format("Permissões insuficientes para acessar '%s'. Permissão necessária: %s",
                recurso, permissaoNecessaria));
    }
}
