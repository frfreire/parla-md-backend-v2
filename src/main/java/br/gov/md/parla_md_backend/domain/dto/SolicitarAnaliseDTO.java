package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SolicitarAnaliseDTO(
        @NotEmpty(message = "Lista de parlamentares não pode estar vazia")
        List<String> parlamentarIds,

        List<String> temasInteresse,

        Boolean incluirPrevisaoVoto,

        @NotBlank(message = "Proposição relacionada é obrigatória")
        String proposicaoRelacionadaId
) {}
