package br.gov.md.parla_md_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrevisaoDTO {

    private String proposicaoId;

    private Double probabilidadeAprovacao;

    private String justificativa;

    private List<Fator> fatoresInfluentes;

    private String confianca;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fator {
        private String nome;
        private String impacto;
        private Double peso;
        private String descricao;
    }
}