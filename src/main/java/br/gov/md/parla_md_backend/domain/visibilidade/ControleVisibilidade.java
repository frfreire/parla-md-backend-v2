package br.gov.md.parla_md_backend.domain.visibilidade;

import br.gov.md.parla_md_backend.domain.enums.NivelVisibilidade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controle de visibilidade de documentos (pareceres e tramitações)
 * Esta classe é embarcada (embedded) nos documentos que necessitam controle de acesso
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControleVisibilidade {

    /**
     * Nível de visibilidade do documento
     */
    @Builder.Default
    private NivelVisibilidade nivelVisibilidade = NivelVisibilidade.PUBLICO;

    /**
     * ID do autor/criador do documento
     */
    private String autorId;

    /**
     * Nome do autor (para facilitar consultas)
     */
    private String autorNome;

    /**
     * Data em que a visibilidade foi definida
     */
    @Builder.Default
    private LocalDateTime dataDefinicao = LocalDateTime.now();

    /**
     * Data da última alteração de visibilidade
     */
    private LocalDateTime dataUltimaAlteracao;

    /**
     * Lista de setores autorizados (para RESTRITO_SETOR)
     */
    @Builder.Default
    private List<String> setoresAutorizados = new ArrayList<>();

    /**
     * Lista de permissões individuais (para RESTRITO_INDIVIDUAL)
     */
    @Builder.Default
    private List<PermissaoAcesso> permissoesIndividuais = new ArrayList<>();

    /**
     * Justificativa para restrição de acesso
     */
    private String justificativaRestricao;

    /**
     * Se permite visualização por superior hierárquico
     */
    @Builder.Default
    private boolean permitirVisualizacaoSuperior = true;

    /**
     * Data de expiração da restrição (opcional)
     * Após esta data, documento volta a ser PUBLICO automaticamente
     */
    private LocalDateTime dataExpiracao;

    /**
     * Verifica se o documento está expirado (deve voltar a PUBLICO)
     */
    public boolean isExpirado() {
        if (dataExpiracao == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(dataExpiracao);
    }

    /**
     * Adiciona setor autorizado
     */
    public void adicionarSetorAutorizado(String setorId) {
        if (setoresAutorizados == null) {
            setoresAutorizados = new ArrayList<>();
        }
        if (!setoresAutorizados.contains(setorId)) {
            setoresAutorizados.add(setorId);
        }
    }

    /**
     * Remove setor autorizado
     */
    public void removerSetorAutorizado(String setorId) {
        if (setoresAutorizados != null) {
            setoresAutorizados.remove(setorId);
        }
    }

    /**
     * Adiciona permissão individual
     */
    public void adicionarPermissao(PermissaoAcesso permissao) {
        if (permissoesIndividuais == null) {
            permissoesIndividuais = new ArrayList<>();
        }
        permissoesIndividuais.add(permissao);
    }

    /**
     * Remove permissão individual (desativa)
     */
    public void removerPermissao(String usuarioId) {
        if (permissoesIndividuais != null) {
            permissoesIndividuais.stream()
                    .filter(p -> p.getUsuarioId().equals(usuarioId))
                    .forEach(p -> p.setAtiva(false));
        }
    }

    /**
     * Busca permissão ativa de um usuário
     */
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