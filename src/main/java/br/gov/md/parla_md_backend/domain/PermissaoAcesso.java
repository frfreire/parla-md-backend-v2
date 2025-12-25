package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.TipoRestricao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissaoAcesso {

    private String usuarioId;

    private String usuarioNome;

    private TipoRestricao tipoPermissao;

    private LocalDateTime dataConcessao;

    private String concedidoPorId;

    private String concedidoPorNome;

    private LocalDateTime dataExpiracao;

    private String justificativa;

    @Builder.Default
    private boolean ativa = true;

     public boolean isExpirada() {
        if (dataExpiracao == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(dataExpiracao);
    }

    public boolean isValida() {
        return ativa && !isExpirada();
    }

}
