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

@Document(collection = "votacoes")
@CompoundIndexes({
        @CompoundIndex(name = "idx_parlamentar_data", def = "{'parlamentarId': 1, 'dataHoraInicio': -1}"),
        @CompoundIndex(name = "idx_proposicao_data", def = "{'proposicaoId': 1, 'dataHoraInicio': -1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Votacao {

    @Id
    private String id;

    @Indexed
    private LocalDateTime dataHoraInicio;

    private LocalDateTime dataHoraFim;

    private String siglaOrgao;

    private String uriProposicaoPrincipal;

    private String descricao;

    @Indexed
    private String parlamentarId;

    @Indexed
    private String proposicaoId;

    @Indexed
    private String materiaId;

    private String voto;

    private LocalDateTime votoData;

    public boolean isVotoFavoravel() {
        return "Sim".equalsIgnoreCase(voto) || "SIM".equalsIgnoreCase(voto);
    }

    public boolean isVotoContrario() {
        return "Não".equalsIgnoreCase(voto) || "NAO".equalsIgnoreCase(voto) ||
                "NÃO".equalsIgnoreCase(voto);
    }

    public boolean isAbstencao() {
        return "Abstenção".equalsIgnoreCase(voto) || "ABSTENCAO".equalsIgnoreCase(voto);
    }
}