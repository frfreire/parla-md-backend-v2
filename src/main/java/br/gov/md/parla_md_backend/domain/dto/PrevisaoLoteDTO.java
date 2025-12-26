package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrevisaoLoteDTO {

    @NotEmpty(message = "Lista de IDs não pode ser vazia")
    @Size(max = 50, message = "Máximo de 50 itens por lote")
    private List<String> itemLegislativoIds;

    private String tipoPrevisao;

    private Boolean forcarNovaPrevisao;

    public boolean isForcarNovaPrevisao() {
        return forcarNovaPrevisao != null && forcarNovaPrevisao;
    }

    public String getTipoPrevisaoOrDefault() {
        return tipoPrevisao != null ? tipoPrevisao : "APROVACAO";
    }
}