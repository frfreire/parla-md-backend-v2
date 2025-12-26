package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "historico_metricas")
@CompoundIndexes({
        @CompoundIndex(name = "idx_tipo_data", def = "{'tipo': 1, 'dataRegistro': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoMetricas {

    @Id
    private String id;

    @Indexed
    private String tipo;

    @Indexed
    private LocalDateTime dataRegistro;

    private String metrica;

    private Double valor;

    private String unidade;

    private String categoria;

    private Map<String, Object> detalhes;
}