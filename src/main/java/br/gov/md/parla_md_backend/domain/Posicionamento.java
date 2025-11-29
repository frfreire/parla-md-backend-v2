package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.TipoPosicionamento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "parladb.positionings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Posicionamento {
    @Id
    private String id;
    private String propositionId;
    private String setorId;
    private TipoPosicionamento tipo;
    private String justificativa;
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataResposta;
    private String usuarioSolicitanteId;
    private String usuarioRespondenteId;
}
