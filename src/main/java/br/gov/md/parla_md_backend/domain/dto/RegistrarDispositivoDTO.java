package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrarDispositivoDTO(
        @NotBlank(message = "Token FCM é obrigatório")
        String tokenFcm,

        @NotBlank(message = "Plataforma é obrigatória")
        String plataforma,

        String modelo,
        String versaoApp
) {}