package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.StatusPosicionamento;
import br.gov.md.parla_md_backend.domain.enums.TipoPosicionamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posicionamentos")
public class Posicionamento {

    @Id
    private String id;

    @Indexed
    private String processoId;

    @Indexed
    private String orgaoExternoId;
    private String orgaoExternoNome;

    private String solicitanteId;
    private String solicitanteNome;

    private TipoPosicionamento tipo;
    private StatusPosicionamento status;

    private String conteudo;
    private String justificativa;
    private String observacoes;

    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataRecebimento;
    private LocalDate prazo;

    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();
    private LocalDateTime dataAtualizacao;
}
