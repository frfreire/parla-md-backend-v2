package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "areas_impacto")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaImpacto {

    @Id
    private String id;

    @Indexed(unique = true)
    private String nome;

    private String descricao;

    private List<String> keywords;

    private List<String> gruposAfetados;

    private String categoria;

    private Boolean ativa;

    private Integer ordem;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataUltimaAtualizacao;

    public boolean contemKeyword(String texto) {
        if (keywords == null || keywords.isEmpty()) {
            return false;
        }

        String textoLower = texto.toLowerCase();
        return keywords.stream()
                .anyMatch(keyword -> textoLower.contains(keyword.toLowerCase()));
    }

    public long contarKeywords(String texto) {
        if (keywords == null || keywords.isEmpty()) {
            return 0;
        }

        String textoLower = texto.toLowerCase();
        return keywords.stream()
                .filter(keyword -> textoLower.contains(keyword.toLowerCase()))
                .count();
    }
}