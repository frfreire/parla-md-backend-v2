package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.interfaces.AnaliseIAEntity;
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
import java.util.Map;

@Document(collection = "analises_parlamentares")
@CompoundIndexes({
        @CompoundIndex(name = "idx_parlamentar_tema", def = "{'parlamentar': 1, 'tema': 1}"),
        @CompoundIndex(name = "idx_tema_data", def = "{'tema': 1, 'dataAnalise': -1}"),
        @CompoundIndex(name = "idx_posicionamento_data", def = "{'posicionamento': 1, 'dataAnalise': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseParlamentar implements AnaliseIAEntity {

    @Id
    private String id;

    @DBRef
    @Indexed
    private Parlamentar parlamentar;

    @Indexed
    private String tema;

    private String posicionamento;
    private Double confiabilidade;
    private String analiseDetalhada;
    private String tendencia;
    private List<String> padroesIdentificados;
    private Map<String, Object> estatisticas;
    private Integer totalVotacoes;
    private Integer votosAFavor;
    private Integer votosContra;
    private Integer abstencoes;
    private Double percentualCoerencia;
    private List<String> votacoesChave;
    private String alinhamentoPolitico;
    private String previsaoComportamento;

    @Indexed
    private LocalDateTime dataAnalise;

    private String modeloVersao;
    private String promptUtilizado;
    private String respostaCompleta;
    private Long tempoProcessamentoMs;
    private Boolean sucesso;
    private String mensagemErro;
    private LocalDateTime dataExpiracao;

    public boolean isPosicionamentoPro() {
        return "PRO".equalsIgnoreCase(posicionamento);
    }

    public boolean isPosicionamentoContra() {
        return "CONTRA".equalsIgnoreCase(posicionamento);
    }

    public boolean isPosicionamentoNeutro() {
        return "NEUTRO".equalsIgnoreCase(posicionamento);
    }

    public boolean isAltaConfiabilidade() {
        return confiabilidade != null && confiabilidade >= 0.8;
    }

    public boolean isMediaConfiabilidade() {
        return confiabilidade != null && confiabilidade >= 0.5 && confiabilidade < 0.8;
    }

    public boolean isBaixaConfiabilidade() {
        return confiabilidade != null && confiabilidade < 0.5;
    }
}