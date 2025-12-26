package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.NivelVisibilidade;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record ControleVisibilidadeDTO(
        @NotNull(message = "Nível de visibilidade é obrigatório")
        NivelVisibilidade nivelVisibilidade,

        List<String> setoresAutorizados,
        List<String> usuariosAutorizados,
        String justificativaRestricao,
        Boolean permitirVisualizacaoSuperior,
        LocalDateTime dataExpiracao
) {
    public ControleVisibilidadeDTO {
        if (permitirVisualizacaoSuperior == null) {
            permitirVisualizacaoSuperior = true;
        }
    }

}