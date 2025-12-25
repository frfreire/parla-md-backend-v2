package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.Email;

import java.util.List;

public record AtualizarSetorDTO(
        String nome,
        String sigla,
        String descricao,
        String responsavelId,

        List<String> competencias,
        List<String> areasAtuacao,

        @Email(message = "E-mail inválido")
        String email,

        String telefone,
        Boolean podeEmitirParecer,
        Boolean recebeTramitacoes
) {}