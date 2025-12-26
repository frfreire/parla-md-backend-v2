package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.OrgaoExterno;

import java.util.List;

public record OrgaoExternoDTO(
        String id,
        String nome,
        String sigla,
        String tipo,
        String descricao,
        String emailOficial,
        String telefone,
        String endereco,
        List<OrgaoExterno.Representante> representantes,
        boolean ativo,
        String observacoes
) {}