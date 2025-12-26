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
public class SolicitarPrevisaoDTO {

    @NotBlank(message = "ID do item legislativo é obrigatório")
    private String itemLegislativoId;

    private String tipoPrevisao;

    private Boolean forcarNovaPrevisao;

    public boolean isForcarNovaPrevisao() {
        return forcarNovaPrevisao != null && forcarNovaPrevisao;
    }

    public String getTipoPrevisaoOrDefault() {
        return tipoPrevisao != null ? tipoPrevisao : "APROVACAO";
    }
}