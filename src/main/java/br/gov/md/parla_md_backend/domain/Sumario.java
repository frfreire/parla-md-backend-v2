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

@Document(collection = "sumarios")
@CompoundIndexes({
        @CompoundIndex(name = "idx_item_data", def = "{'itemLegislativo': 1, 'dataCriacao': -1}"),
        @CompoundIndex(name = "idx_tipo_data", def = "{'tipoSumario': 1, 'dataCriacao': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sumario {

    @Id
    private String id;

    @DBRef
    @Indexed
    private ItemLegislativo itemLegislativo;

    private String tipoSumario;

    private String sumarioExecutivo;

    private List<String> pontosPrincipais;

    private List<String> entidadesRelevantes;

    private List<String> palavrasChave;

    private String temasPrincipais;

    private String sentimentoGeral;

    private String impactoEstimado;

    private String observacoes;

    @Indexed
    private LocalDateTime dataCriacao;

    private String modeloVersao;

    private String promptUtilizado;

    private String respostaCompleta;

    private Long tempoProcessamentoMs;

    private Integer tamanhoTextoOriginal;

    private Integer tamanhoSumario;

    private Double taxaCompressao;

    private Boolean sucesso;

    private String mensagemErro;

    private LocalDateTime dataExpiracao;

    public double calcularTaxaCompressao() {
        if (tamanhoTextoOriginal == null || tamanhoTextoOriginal == 0) {
            return 0.0;
        }
        if (tamanhoSumario == null) {
            return 0.0;
        }
        return (double) tamanhoSumario / tamanhoTextoOriginal;
    }

    public boolean isCompressaoEficiente() {
        return taxaCompressao != null && taxaCompressao < 0.3;
    }

    public boolean isCompressaoExcessiva() {
        return taxaCompressao != null && taxaCompressao < 0.1;
    }
}