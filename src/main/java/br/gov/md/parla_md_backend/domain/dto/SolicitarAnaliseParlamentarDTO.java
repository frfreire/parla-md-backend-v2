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
public class SolicitarAnaliseParlamentarDTO {

    @NotBlank(message = "ID do parlamentar é obrigatório")
    private String parlamentarId;

    @NotBlank(message = "Tema é obrigatório")
    private String tema;

    private Boolean incluirEstatisticas;

    private Boolean incluirTendencias;

    private Boolean incluirPrevisoes;

    private Boolean forcarNovaAnalise;

    public boolean isIncluirEstatisticas() {
        return incluirEstatisticas != null && incluirEstatisticas;
    }

    public boolean isIncluirTendencias() {
        return incluirTendencias != null && incluirTendencias;
    }

    public boolean isIncluirPrevisoes() {
        return incluirPrevisoes != null && incluirPrevisoes;
    }

    public boolean isForcarNovaAnalise() {
        return forcarNovaAnalise != null && forcarNovaAnalise;
    }
}
