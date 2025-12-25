package br.gov.md.parla_md_backend.domain.dto.old;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequisicaoLlamaDTO {

    private String model;

    private List<Mensagem> messages;

    private Boolean stream;

    @JsonProperty("format")
    private String format;

    private Options options;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Mensagem {
        private String role;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Options {
        private Double temperature;

        @JsonProperty("top_p")
        private Double topP;

        @JsonProperty("top_k")
        private Integer topK;

        @JsonProperty("num_predict")
        private Integer numPredict;
    }
}