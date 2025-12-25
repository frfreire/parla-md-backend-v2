package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CriarSetorDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "Sigla é obrigatória")
        String sigla,

        String descricao,
        String setorPaiId,
        String responsavelId,
        String responsavelNome,

        List<String> competencias,
        List<String> areasAtuacao,

        @Email(message = "E-mail inválido")
        String email,

        String telefone,
        Boolean podeEmitirParecer,
        Boolean recebeTramitacoes
) {}