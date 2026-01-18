package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record PromptRequest(

        @NotBlank(message = "Prompt é obrigatório")
        String prompt,

        Boolean stream
) {
    public PromptRequest {
        if (stream == null) {
            stream = false;
        }
    }
}