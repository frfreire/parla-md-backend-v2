package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExtrairJsonRequest(

        @NotNull(message = "Resposta do Llama é obrigatória")
        RespostaLlamaDTO resposta,

        @NotBlank(message = "Classe alvo é obrigatória")
        String classeAlvo
) {}