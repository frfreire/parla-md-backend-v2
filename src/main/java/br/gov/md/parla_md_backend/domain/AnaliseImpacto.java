package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "analises_impacto")
@CompoundIndexes({
        @CompoundIndex(name = "idx_item_area", def = "{'itemLegislativo': 1, 'areaImpacto': 1}"),
        @CompoundIndex(name = "idx_area_data", def = "{'areaImpacto': 1, 'dataAnalise': -1}"),
        @CompoundIndex(name = "idx_nivel_data", def = "{'nivelImpacto': 1, 'dataAnalise': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseImpacto {

    @Id
    private String id;

    @DBRef
    @Indexed
    private ItemLegislativo itemLegislativo;

    @DBRef
    @Indexed
    private AreaImpacto areaImpacto;

    private String nivelImpacto;

    private String tipoImpacto;

    private Double percentualImpacto;

    private String analiseDetalhada;

    private List<String> consequencias;

    private List<String> gruposAfetados;

    private List<String> riscos;

    private List<String> oportunidades;

    private String recomendacoes;

    @Indexed
    private LocalDateTime dataAnalise;

    private String modeloVersao;

    private String promptUtilizado;

    private String respostaCompleta;

    private Long tempoProcessamentoMs;

    private Boolean sucesso;

    private String mensagemErro;

    private LocalDateTime dataExpiracao;

    public boolean isImpactoAlto() {
        return "ALTO".equalsIgnoreCase(nivelImpacto);
    }

    public boolean isImpactoMedio() {
        return "MEDIO".equalsIgnoreCase(nivelImpacto);
    }

    public boolean isImpactoBaixo() {
        return "BAIXO".equalsIgnoreCase(nivelImpacto);
    }

    public boolean isImpactoNegativo() {
        return "NEGATIVO".equalsIgnoreCase(tipoImpacto);
    }

    public boolean isImpactoPositivo() {
        return "POSITIVO".equalsIgnoreCase(tipoImpacto);
    }
}