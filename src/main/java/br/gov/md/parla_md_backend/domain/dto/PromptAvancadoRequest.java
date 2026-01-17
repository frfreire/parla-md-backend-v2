package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record PromptAvancadoRequest(

        @NotBlank(message = "Prompt do usuário é obrigatório")
        String promptUsuario,

        String promptSistema,

        Boolean respostaEmJson,

        Boolean stream
) {
    public PromptAvancadoRequest {
        if (respostaEmJson == null) {
            respostaEmJson = false;
        }
        if (stream == null) {
            stream = false;
        }
    }
}