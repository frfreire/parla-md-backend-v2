package br.gov.md.parla_md_backend.exception;

/**
 * Exceção lançada quando um usuário não possui permissão para acessar um recurso específico.
 * 
 * <p>Esta exceção é utilizada em situações onde:</p>
 * <ul>
 *   <li>Usuário não possui role necessária para o endpoint</li>
 *   <li>Usuário não tem permissão específica para o recurso</li>
 *   <li>Tentativa de acesso a funcionalidade restrita</li>
 *   <li>Operação não permitida para o nível de acesso do usuário</li>
 * </ul>
 * 
 * @author fabricio.freire
 * @version 1.0
 * @since 2024-12-16
 */
public class PermissaoNegadaException extends AutenticacaoException {
    
    private static final long serialVersionUID = 1L;
    private static final String CODIGO_ERRO = "AUTH003";
    
    private final String recurso;
    private final String permissaoNecessaria;
    
    /**
     * Construtor padrão com mensagem genérica.
     */
    public PermissaoNegadaException() {
        super("[" + CODIGO_ERRO + "] Acesso negado. Você não possui permissão para este recurso.");
        this.recurso = null;
        this.permissaoNecessaria = null;
    }
    
    /**
     * Construtor com mensagem personalizada.
     * 
     * @param mensagem a mensagem de erro específica
     */
    public PermissaoNegadaException(String mensagem) {
        super("[" + CODIGO_ERRO + "] " + mensagem);
        this.recurso = null;
        this.permissaoNecessaria = null;
    }
    
    /**
     * Construtor com recurso e permissão específicos.
     * 
     * @param recurso o recurso que foi negado acesso
     * @param permissaoNecessaria a permissão necessária para acessar o recurso
     */
    public PermissaoNegadaException(String recurso, String permissaoNecessaria) {
        super("[" + CODIGO_ERRO + "] " + String.format("Acesso negado ao recurso '%s'. Permissão necessária: '%s'", 
                                         recurso, permissaoNecessaria));
        this.recurso = recurso;
        this.permissaoNecessaria = permissaoNecessaria;
    }
    
    /**
     * Construtor com recurso, permissão e mensagem personalizada.
     * 
     * @param recurso o recurso que foi negado acesso
     * @param permissaoNecessaria a permissão necessária
     * @param mensagem mensagem personalizada
     */
    public PermissaoNegadaException(String recurso, String permissaoNecessaria, String mensagem) {
        super("[" + CODIGO_ERRO + "] " + mensagem);
        this.recurso = recurso;
        this.permissaoNecessaria = permissaoNecessaria;
    }
    
    /**
     * Construtor com mensagem e causa da exceção.
     * 
     * @param mensagem a mensagem de erro
     * @param causa a causa raiz da exceção
     */
    public PermissaoNegadaException(String mensagem, Throwable causa) {
        super("[" + CODIGO_ERRO + "] " + mensagem, causa);
        this.recurso = null;
        this.permissaoNecessaria = null;
    }
    
    /**
     * Cria uma exceção para role insuficiente.
     * 
     * @param roleNecessaria a role necessária
     * @return PermissaoNegadaException configurada para role insuficiente
     */
    public static PermissaoNegadaException roleInsuficiente(String roleNecessaria) {
        return new PermissaoNegadaException(
            String.format("Acesso negado. Role necessária: '%s'", roleNecessaria)
        );
    }
    
    /**
     * Cria uma exceção para operação administrativa.
     * 
     * @return PermissaoNegadaException configurada para operação administrativa
     */
    public static PermissaoNegadaException operacaoAdministrativa() {
        return new PermissaoNegadaException(
            "Acesso negado. Esta operação requer privilégios administrativos."
        );
    }
    
    /**
     * Cria uma exceção para recurso específico.
     * 
     * @param nomeRecurso o nome do recurso negado
     * @return PermissaoNegadaException configurada para o recurso específico
     */
    public static PermissaoNegadaException recursoNegado(String nomeRecurso) {
        return new PermissaoNegadaException(
            String.format("Acesso negado ao recurso: '%s'", nomeRecurso)
        );
    }
    
    /**
     * Obtém o recurso que foi negado acesso.
     * 
     * @return o nome do recurso ou null se não especificado
     */
    public String getRecurso() {
        return recurso;
    }
    
    /**
     * Obtém a permissão necessária para acessar o recurso.
     * 
     * @return a permissão necessária ou null se não especificada
     */
    public String getPermissaoNecessaria() {
        return permissaoNecessaria;
    }
}