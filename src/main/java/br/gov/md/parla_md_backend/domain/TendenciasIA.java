package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.interfaces.AnaliseIAEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "tendencias_ia")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TendenciasIA implements AnaliseIAEntity {

    @Id
    private String id;

    private String analiseGeral;

    private List<String> temasEmergentes;

    private List<String> alertas;

    private String previsaoProximoMes;

    private Map<String, Object> dadosContexto;

    private Integer totalDocumentosAnalisados;

    private String periodoReferencia;

    @Indexed
    private LocalDateTime dataAnalise;

    private String modeloVersao;

    private String promptUtilizado;

    private String respostaCompleta;

    private Long tempoProcessamentoMs;

    private Boolean sucesso;

    private String mensagemErro;

    private LocalDateTime dataExpiracao;
}