package br.gov.md.parla_md_backend.domain.visibilidade;

import br.gov.md.parla_md_backend.domain.enums.NivelVisibilidade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControleVisibilidade {

    @Builder.Default
    private NivelVisibilidade nivelVisibilidade = NivelVisibilidade.PUBLICO;

    private String autorId;

    private String autorNome;

    @Builder.Default
    private LocalDateTime dataDefinicao = LocalDateTime.now();

    private LocalDateTime dataUltimaAlteracao;

    @Builder.Default
    private List<String> setoresAutorizados = new ArrayList<>();

    @Builder.Default
    private List<PermissaoAcesso> permissoesIndividuais = new ArrayList<>();

    private String justificativaRestricao;

    @Builder.Default
    private boolean permitirVisualizacaoSuperior = true;

    private LocalDateTime dataExpiracao;

    public boolean isExpirado() {
        if (dataExpiracao == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(dataExpiracao);
    }

    public void adicionarSetorAutorizado(String setorId) {
        if (setoresAutorizados == null) {
            setoresAutorizados = new ArrayList<>();
        }
        if (!setoresAutorizados.contains(setorId)) {
            setoresAutorizados.add(setorId);
        }
    }

    public void removerSetorAutorizado(String setorId) {
        if (setoresAutorizados != null) {
            setoresAutorizados.remove(setorId);
        }
    }

    public void adicionarPermissao(PermissaoAcesso permissao) {
        if (permissoesIndividuais == null) {
            permissoesIndividuais = new ArrayList<>();
        }
        permissoesIndividuais.add(permissao);
    }

    public void removerPermissao(String usuarioId) {
        if (permissoesIndividuais != null) {
            permissoesIndividuais.stream()
                    .filter(p -> p.getUsuarioId().equals(usuarioId))
                    .forEach(p -> p.setAtiva(false));
        }
    }

    public PermissaoAcesso buscarPermissaoUsuario(String usuarioId) {
        if (permissoesIndividuais == null) {
            return null;
        }
        return permissoesIndividuais.stream()
                .filter(p -> p.getUsuarioId().equals(usuarioId))
                .filter(PermissaoAcesso::isValida)
                .findFirst()
                .orElse(null);
    }
}