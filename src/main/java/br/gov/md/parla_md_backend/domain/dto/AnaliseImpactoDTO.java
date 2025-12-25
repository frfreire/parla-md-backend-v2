package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public record AnaliseImpactoDTO(
        @NotBlank(message = "ID da proposição é obrigatório")
        String proposicaoId,
        String descricaoProposicao,
        String impactoGeral,
        String gravidade,
        List<ImpactoSetorialDTO> impactosSetoriais,
        Map<String, String> impactosEspecificos,
        List<String> riscos,
        List<String> oportunidades,
        String recomendacaoPosicionamento,
        String fundamentacao,
        Double scorePrioridade,
        Boolean requerAnaliseDetalhada
) {}