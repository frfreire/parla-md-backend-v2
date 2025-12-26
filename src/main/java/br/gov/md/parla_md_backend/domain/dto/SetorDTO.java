package br.gov.md.parla_md_backend.domain.dto;

import java.util.List;

public record SetorDTO(
        String id,
        String nome,
        String sigla,
        String descricao,
        String setorPaiId,
        Integer nivel,
        String responsavelId,
        String responsavelNome,
        List<String> competencias,
        List<String> areasAtuacao,
        String email,
        String telefone,
        boolean ativo,
        boolean podeEmitirParecer,
        boolean recebeTramitacoes
) {}
