package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.OrgaoExterno;
import jakarta.validation.constraints.Email;

import java.util.List;

public record AtualizarOrgaoExternoDTO(
        String nome,
        String sigla,
        String tipo,
        String descricao,

        @Email(message = "E-mail inválido")
        String emailOficial,

        String telefone,
        String endereco,

        List<OrgaoExterno.Representante> representantes,
        String observacoes
) {}