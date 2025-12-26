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

@Document(collection = "previsoes")
@CompoundIndexes({
        @CompoundIndex(name = "idx_item_data", def = "{'itemLegislativo': 1, 'dataPrevisao': -1}"),
        @CompoundIndex(name = "idx_tipo_data", def = "{'tipoPrevisao': 1, 'dataPrevisao': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Previsao {

    @Id
    private String id;

    @DBRef
    @Indexed
    private ItemLegislativo itemLegislativo;

    private String tipoPrevisao;

    private Double probabilidadeAprovacao;

    private Double confianca;

    private String justificativa;

    private String fatoresPositivos;

    private String fatoresNegativos;

    @Indexed
    private LocalDateTime dataPrevisao;

    private String modeloVersao;

    private String promptUtilizado;

    private String respostaCompleta;

    private Long tempoProcessamentoMs;

    private Boolean sucesso;

    private String mensagemErro;

    private LocalDateTime dataExpiracao;

    public boolean isAltaConfianca() {
        return confianca != null && confianca >= 0.8;
    }

    public boolean isMediaConfianca() {
        return confianca != null && confianca >= 0.5 && confianca < 0.8;
    }

    public boolean isBaixaConfianca() {
        return confianca != null && confianca < 0.5;
    }

    public String getClassificacao() {
        if (probabilidadeAprovacao == null) return "INDEFINIDO";
        if (probabilidadeAprovacao >= 0.7) return "MUITO_PROVAVEL";
        if (probabilidadeAprovacao >= 0.5) return "PROVAVEL";
        if (probabilidadeAprovacao >= 0.3) return "IMPROVAVEL";
        return "MUITO_IMPROVAVEL";
    }
}
