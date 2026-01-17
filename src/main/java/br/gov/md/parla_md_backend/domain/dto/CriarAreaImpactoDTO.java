package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CriarAreaImpactoDTO(

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
        String nome,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String descricao,

        List<String> keywords,

        List<String> gruposAfetados,

        @Size(max = 50, message = "Categoria deve ter no máximo 50 caracteres")
        String categoria,

        Boolean ativa,

        Integer ordem
) {
    public CriarAreaImpactoDTO {
        if (ativa == null) {
            ativa = true;
        }
        if (ordem == null) {
            ordem = 0;
        }
    }
}