package br.gov.md.parla_md_backend.domain.visibilidade;

import br.gov.md.parla_md_backend.domain.enums.TipoRestricao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Permissão individual de acesso a documento
 * Embarcada em ControleVisibilidade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissaoAcesso {

    /**
     * ID do usuário que recebe a permissão
     */
    private String usuarioId;

    /**
     * Nome do usuário (para facilitar consultas)
     */
    private String usuarioNome;

    /**
     * Tipo de permissão concedida
     */
    private TipoRestricao tipoPermissao;

    /**
     * Data em que a permissão foi concedida
     */
    private LocalDateTime dataConcessao;

    /**
     * ID de quem concedeu a permissão
     */
    private String concedidoPorId;

    /**
     * Nome de quem concedeu
     */
    private String concedidoPorNome;

    /**
     * Data de expiração da permissão (opcional)
     */
    private LocalDateTime dataExpiracao;

    /**
     * Justificativa para concessão da permissão
     */
    private String justificativa;

    /**
     * Se a permissão está ativa
     */
    @Builder.Default
    private boolean ativa = true;

    /**
     * Verifica se a permissão está expirada
     */
    public boolean isExpirada() {
        if (dataExpiracao == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(dataExpiracao);
    }

    /**
     * Verifica se a permissão está válida (ativa e não expirada)
     */
    public boolean isValida() {
        return ativa && !isExpirada();
    }
}
