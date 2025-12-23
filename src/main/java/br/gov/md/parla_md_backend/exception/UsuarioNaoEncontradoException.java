package br.gov.md.parla_md_backend.exception;

public class UsuarioNaoEncontradoException extends AutenticacaoException {
    
    private static final long serialVersionUID = 1L;
    private static final String CODIGO_ERRO = "AUTH004";
    
    private final String identificadorUsuario;
    private final String tipoIdentificador;
    
    public UsuarioNaoEncontradoException() {
        super("[" + CODIGO_ERRO + "] Usuário não encontrado no sistema");
        this.identificadorUsuario = null;
        this.tipoIdentificador = null;
    }
    

    public UsuarioNaoEncontradoException(String identificadorUsuario) {
        super("[" + CODIGO_ERRO + "] " + String.format("Usuário não encontrado: '%s'", identificadorUsuario));
        this.identificadorUsuario = identificadorUsuario;
        this.tipoIdentificador = "desconhecido";
    }
    
    public UsuarioNaoEncontradoException(String identificadorUsuario, String tipoIdentificador) {
        super("[" + CODIGO_ERRO + "] " + String.format("Usuário não encontrado por %s: '%s'", 
                                         tipoIdentificador, identificadorUsuario));
        this.identificadorUsuario = identificadorUsuario;
        this.tipoIdentificador = tipoIdentificador;
    }
    
    public UsuarioNaoEncontradoException(String mensagem, Throwable causa) {
        super("[" + CODIGO_ERRO + "] " + mensagem, causa);
        this.identificadorUsuario = null;
        this.tipoIdentificador = null;
    }
    
    public static UsuarioNaoEncontradoException porId(String userId) {
        return new UsuarioNaoEncontradoException(userId, "ID");
    }
    
    public static UsuarioNaoEncontradoException porUsername(String username) {
        return new UsuarioNaoEncontradoException(username, "username");
    }
    
    public static UsuarioNaoEncontradoException porEmail(String email) {
        return new UsuarioNaoEncontradoException(email, "email");
    }
    
    public static UsuarioNaoEncontradoException usuarioDesativado(String identificador) {
        return new UsuarioNaoEncontradoException(
            String.format("Usuário '%s' foi desativado ou removido do sistema", identificador)
        );
    }
    
    public static UsuarioNaoEncontradoException subjectInvalido(String subject) {
        return new UsuarioNaoEncontradoException(subject, "subject (sub) do JWT");
    }
    
    public String getIdentificadorUsuario() {
        return identificadorUsuario;
    }
    
    public String getTipoIdentificador() {
        return tipoIdentificador;
    }
}