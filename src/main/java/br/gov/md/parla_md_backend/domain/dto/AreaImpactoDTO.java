package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.AreaImpacto;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record AreaImpactoDTO(
        String id,
        String nome,
        String descricao,
        List<String> keywords,
        List<String> gruposAfetados,
        String categoria,
        Boolean ativa,
        Integer ordem,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime dataCriacao,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime dataUltimaAtualizacao
) {
    public static AreaImpactoDTO from(AreaImpacto area) {
        if (area == null) {
            return null;
        }

        return new AreaImpactoDTO(
                area.getId(),
                area.getNome(),
                area.getDescricao(),
                area.getKeywords(),
                area.getGruposAfetados(),
                area.getCategoria(),
                area.getAtiva(),
                area.getOrdem(),
                area.getDataCriacao(),
                area.getDataUltimaAtualizacao()
        );
    }

    public AreaImpacto toEntity() {
        return AreaImpacto.builder()
                .id(this.id)
                .nome(this.nome)
                .descricao(this.descricao)
                .keywords(this.keywords)
                .gruposAfetados(this.gruposAfetados)
                .categoria(this.categoria)
                .ativa(this.ativa)
                .ordem(this.ordem)
                .dataCriacao(this.dataCriacao)
                .dataUltimaAtualizacao(this.dataUltimaAtualizacao)
                .build();
    }
}