package br.gov.md.parla_md_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespostaLlamaDTO {

    private String model;
    private String createdAt;
    private Message message;
    private Boolean done;
    private Long totalDuration;
    private Long loadDuration;
    private Integer promptEvalCount;
    private Long promptEvalDuration;
    private Integer evalCount;
    private Long evalDuration;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}