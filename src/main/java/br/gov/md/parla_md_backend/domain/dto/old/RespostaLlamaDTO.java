package br.gov.md.parla_md_backend.domain.dto.old;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
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
    public static class Mensagem {
        private String role;
        private String content;
    }
}