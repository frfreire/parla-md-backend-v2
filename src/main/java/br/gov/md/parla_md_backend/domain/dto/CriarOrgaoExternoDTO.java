package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.OrgaoExterno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CriarOrgaoExternoDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "Sigla é obrigatória")
        String sigla,

        @NotNull(message = "Tipo é obrigatório")
        String tipo,

        String descricao,

        @Email(message = "E-mail inválido")
        String emailOficial,

        String telefone,
        String endereco,

        List<OrgaoExterno.Representante> representantes,
        String observacoes
) {}