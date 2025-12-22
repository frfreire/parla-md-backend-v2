package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.TipoRestricao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ConcederPermissaoDTO(
        @NotBlank(message = "ID do usuário é obrigatório")
        String usuarioId,

        @NotBlank(message = "Nome do usuário é obrigatório")
        String usuarioNome,

        @NotNull(message = "Tipo de permissão é obrigatório")
        TipoRestricao tipoPermissao,

        LocalDateTime dataExpiracao,
        String justificativa
) {}