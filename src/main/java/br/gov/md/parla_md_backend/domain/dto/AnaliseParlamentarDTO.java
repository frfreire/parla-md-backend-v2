package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.AlinhamentoPolitico;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AnaliseParlamentarDTO(
        String parlamentarId,
        String nomeParlamentar,
        String partido,
        String uf,
        String casa,

        AlinhamentoPolitico alinhamentoGoverno,
        Double percentualAlinhamentoGoverno,

        List<PosicionamentoTematicoDTO> posicionamentosTematicos,

        MetricasDesempenhoDTO metricas,

        String resumoIA,
        List<String> pontosFortes,
        List<String> areasInteresse,
        String estrategiaAbordagem,

        Integer totalVotacoes,
        Integer votacoesDefesa,

        LocalDateTime dataUltimaAnalise,
        String versaoModeloIA
) {}