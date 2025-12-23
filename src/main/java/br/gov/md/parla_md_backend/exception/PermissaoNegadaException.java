package br.gov.md.parla_md_backend.exception;

public class PermissaoNegadaException extends AutenticacaoException {
    
    private static final long serialVersionUID = 1L;
    private static final String CODIGO_ERRO = "AUTH003";
    
    private final String recurso;
    private final String permissaoNecessaria;
    
    public PermissaoNegadaException() {
        super("[" + CODIGO_ERRO + "] Acesso negado. Você não possui permissão para este recurso.");
        this.recurso = null;
        this.permissaoNecessaria = null;
    }
    
    public PermissaoNegadaException(String mensagem) {
        super("[" + CODIGO_ERRO + "] " + mensagem);
        this.recurso = null;
        this.permissaoNecessaria = null;
    }
    
    public PermissaoNegadaException(String recurso, String permissaoNecessaria) {
        super("[" + CODIGO_ERRO + "] " + String.format("Acesso negado ao recurso '%s'. Permissão necessária: '%s'", 
                                         recurso, permissaoNecessaria));
        this.recurso = recurso;
        this.permissaoNecessaria = permissaoNecessaria;
    }
    
    public PermissaoNegadaException(String recurso, String permissaoNecessaria, String mensagem) {
        super("[" + CODIGO_ERRO + "] " + mensagem);
        this.recurso = recurso;
        this.permissaoNecessaria = permissaoNecessaria;
    }
    
    public PermissaoNegadaException(String mensagem, Throwable causa) {
        super("[" + CODIGO_ERRO + "] " + mensagem, causa);
        this.recurso = null;
        this.permissaoNecessaria = null;
    }
    
    public static PermissaoNegadaException roleInsuficiente(String roleNecessaria) {
        return new PermissaoNegadaException(
            String.format("Acesso negado. Role necessária: '%s'", roleNecessaria)
        );
    }
    
    public static PermissaoNegadaException operacaoAdministrativa() {
        return new PermissaoNegadaException(
            "Acesso negado. Esta operação requer privilégios administrativos."
        );
    }
    
    public static PermissaoNegadaException recursoNegado(String nomeRecurso) {
        return new PermissaoNegadaException(
            String.format("Acesso negado ao recurso: '%s'", nomeRecurso)
        );
    }
    
    public String getRecurso() {
        return recurso;
    }
    
    public String getPermissaoNecessaria() {
        return permissaoNecessaria;
    }
}