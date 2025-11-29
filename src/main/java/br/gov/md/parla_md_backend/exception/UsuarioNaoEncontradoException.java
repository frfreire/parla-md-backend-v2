package br.gov.md.parla_md_backend.exception;

/**
 * Exceção lançada quando um usuário não é encontrado no sistema.
 * 
 * <p>Esta exceção é utilizada em situações onde:</p>
 * <ul>
 *   <li>Usuário não existe no Keycloak</li>
 *   <li>ID do usuário não é válido</li>
 *   <li>Username ou email não encontrado</li>
 *   <li>Usuário foi removido ou desativado</li>
 *   <li>Token contém subject (sub) inexistente</li>
 * </ul>
 * 
 * @author fabricio.freire
 * @version 1.0
 * @since 2024-12-16
 */
public class UsuarioNaoEncontradoException extends AutenticacaoException {
    
    private static final long serialVersionUID = 1L;
    private static final String CODIGO_ERRO = "AUTH004";
    
    private final String identificadorUsuario;
    private final String tipoIdentificador;
    
    /**
     * Construtor padrão com mensagem genérica.
     */
    public UsuarioNaoEncontradoException() {
        super("[" + CODIGO_ERRO + "] Usuário não encontrado no sistema");
        this.identificadorUsuario = null;
        this.tipoIdentificador = null;
    }
    
    /**
     * Construtor com mensagem personalizada.
     * 
     * @param mensagem a mensagem de erro específica
     */
//    public UsuarioNaoEncontradoException(String mensagem) {
//        super(CODIGO_ERRO, mensagem);
//        this.identificadorUsuario = null;
//        this.tipoIdentificador = null;
//    }
    
    /**
     * Construtor com identificador do usuário.
     * 
     * @param identificadorUsuario o identificador do usuário (ID, username, email)
     */
    public UsuarioNaoEncontradoException(String identificadorUsuario) {
        super("[" + CODIGO_ERRO + "] " + String.format("Usuário não encontrado: '%s'", identificadorUsuario));
        this.identificadorUsuario = identificadorUsuario;
        this.tipoIdentificador = "desconhecido";
    }
    
    /**
     * Construtor com identificador e tipo específicos.
     * 
     * @param identificadorUsuario o identificador do usuário
     * @param tipoIdentificador o tipo do identificador (id, username, email, etc.)
     */
    public UsuarioNaoEncontradoException(String identificadorUsuario, String tipoIdentificador) {
        super("[" + CODIGO_ERRO + "] " + String.format("Usuário não encontrado por %s: '%s'", 
                                         tipoIdentificador, identificadorUsuario));
        this.identificadorUsuario = identificadorUsuario;
        this.tipoIdentificador = tipoIdentificador;
    }
    
    /**
     * Construtor com mensagem e causa da exceção.
     * 
     * @param mensagem a mensagem de erro
     * @param causa a causa raiz da exceção
     */
    public UsuarioNaoEncontradoException(String mensagem, Throwable causa) {
        super("[" + CODIGO_ERRO + "] " + mensagem, causa);
        this.identificadorUsuario = null;
        this.tipoIdentificador = null;
    }
    
    /**
     * Cria uma exceção para usuário não encontrado por ID.
     * 
     * @param userId o ID do usuário
     * @return UsuarioNaoEncontradoException configurada para ID
     */
    public static UsuarioNaoEncontradoException porId(String userId) {
        return new UsuarioNaoEncontradoException(userId, "ID");
    }
    
    /**
     * Cria uma exceção para usuário não encontrado por username.
     * 
     * @param username o username do usuário
     * @return UsuarioNaoEncontradoException configurada para username
     */
    public static UsuarioNaoEncontradoException porUsername(String username) {
        return new UsuarioNaoEncontradoException(username, "username");
    }
    
    /**
     * Cria uma exceção para usuário não encontrado por email.
     * 
     * @param email o email do usuário
     * @return UsuarioNaoEncontradoException configurada para email
     */
    public static UsuarioNaoEncontradoException porEmail(String email) {
        return new UsuarioNaoEncontradoException(email, "email");
    }
    
    /**
     * Cria uma exceção para usuário desativado.
     * 
     * @param identificador o identificador do usuário
     * @return UsuarioNaoEncontradoException configurada para usuário desativado
     */
    public static UsuarioNaoEncontradoException usuarioDesativado(String identificador) {
        return new UsuarioNaoEncontradoException(
            String.format("Usuário '%s' foi desativado ou removido do sistema", identificador)
        );
    }
    
    /**
     * Cria uma exceção para subject do JWT inválido.
     * 
     * @param subject o subject (sub) do JWT
     * @return UsuarioNaoEncontradoException configurada para subject inválido
     */
    public static UsuarioNaoEncontradoException subjectInvalido(String subject) {
        return new UsuarioNaoEncontradoException(subject, "subject (sub) do JWT");
    }
    
    /**
     * Obtém o identificador do usuário que não foi encontrado.
     * 
     * @return o identificador do usuário ou null se não especificado
     */
    public String getIdentificadorUsuario() {
        return identificadorUsuario;
    }
    
    /**
     * Obtém o tipo do identificador usado na busca.
     * 
     * @return o tipo do identificador ou null se não especificado
     */
    public String getTipoIdentificador() {
        return tipoIdentificador;
    }
}