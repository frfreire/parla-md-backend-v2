package br.gov.md.parla_md_backend.domain.dto;

import java.util.List;


public record ImpactoSetorialDTO(
        String setor,
        String descricaoImpacto,
        String intensidade,
        List<String> detalhes
) {}