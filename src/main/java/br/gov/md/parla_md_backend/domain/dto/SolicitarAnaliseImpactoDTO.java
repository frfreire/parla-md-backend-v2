package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitarAnaliseImpactoDTO {

    @NotBlank(message = "ID do item legislativo é obrigatório")
    private String itemLegislativoId;

    private List<String> areaIds;

    private Boolean analisarTodasAreas;

    private Boolean forcarNovaAnalise;

    public boolean isAnalisarTodasAreas() {
        return analisarTodasAreas != null && analisarTodasAreas;
    }

    public boolean isForcarNovaAnalise() {
        return forcarNovaAnalise != null && forcarNovaAnalise;
    }
}