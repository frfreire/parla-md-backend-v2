package br.gov.md.parla_md_backend.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespostaLlamaDTO {

    private String model;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    private Mensagem message;

    private Boolean done;

    @JsonProperty("total_duration")
    private Long totalDuration;

    @JsonProperty("load_duration")
    private Long loadDuration;

    @JsonProperty("prompt_eval_count")
    private Integer promptEvalCount;

    @JsonProperty("eval_count")
    private Integer evalCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Mensagem {
        private String role;
        private String content;
    }

    public boolean isCompleta() {
        return done != null && done && message != null && message.getContent() != null;
    }

    public String obterConteudo() {
        return message != null ? message.getContent() : null;
    }

    public long getDuracaoTotalMs() {
        return totalDuration != null ? totalDuration / 1_000_000 : 0L;
    }

    public long getDuracaoCarregamentoMs() {
        return loadDuration != null ? loadDuration / 1_000_000 : 0L;
    }
}