package br.gov.md.parla_md_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistribuicaoDTO {

    private String titulo;

    private Map<String, Long> dados;

    private Long total;

    public Long getTotal() {
        if (total == null && dados != null) {
            return dados.values().stream().mapToLong(Long::longValue).sum();
        }
        return total;
    }
}