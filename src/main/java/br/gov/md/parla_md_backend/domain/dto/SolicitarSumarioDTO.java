package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    private String tipoSumario;

    @Size(max = 5000, message = "Texto não pode exceder 5000 caracteres")
    private String textoCustomizado;

    private Boolean incluirPalavrasChave;

    private Boolean incluirEntidades;

    private Boolean incluirSentimento;

    private Boolean forcarNovoSumario;

    public boolean isIncluirPalavrasChave() {
        return incluirPalavrasChave != null && incluirPalavrasChave;
    }

    public boolean isIncluirEntidades() {
        return incluirEntidades != null && incluirEntidades;
    }

    public boolean isIncluirSentimento() {
        return incluirSentimento != null && incluirSentimento;
    }

    public boolean isForcarNovoSumario() {
        return forcarNovoSumario != null && forcarNovoSumario;
    }

    public String getTipoSumarioOrDefault() {
        return tipoSumario != null ? tipoSumario : "EXECUTIVO";
    }
}