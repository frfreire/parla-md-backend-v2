package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.TipoPosicionamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RegistrarPosicionamentoDTO(
        @NotBlank(message = "ID do posicionamento é obrigatório")
        String posicionamentoId,

        @NotNull(message = "Tipo de posicionamento é obrigatório")
        TipoPosicionamento tipo,

        @NotBlank(message = "Conteúdo é obrigatório")
        String conteudo,

        String justificativa,

        List<String> fundamentacao,
        List<String> condicoesRessalvas,

        String impactoEstimado,
        String representanteNome,
        String representanteCargo,
        String numeroOficio,
        String documentoOficial,

        List<String> anexos,
        String observacoes
) {}