package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitarSumarioDTO {

    @NotBlank(message = "ID do item legislativo é obrigatório")
    private String itemLegislativoId;

    private String texto;

    private String tipoSumario;

    private boolean incluirPalavrasChave;

    private boolean incluirEntidades;

    private boolean incluirSentimento;

    private boolean forcarNovoSumario;
}