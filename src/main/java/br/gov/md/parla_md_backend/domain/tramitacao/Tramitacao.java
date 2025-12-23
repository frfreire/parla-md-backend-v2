package br.gov.md.parla_md_backend.domain.tramitacao;

import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
import br.gov.md.parla_md_backend.domain.enums.TipoTramitacao;
import br.gov.md.parla_md_backend.domain.visibilidade.ControleVisibilidade;
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
@Document(collection = "tramitacoes")
public class Tramitacao {

    @Id
    private String id;

    @Indexed
    private String processoId;

    private TipoTramitacao tipo;

    @Indexed
    private String remetenteId;
    private String remetenteTipo;
    private String remetenteNome;

    @Indexed
    private String destinatarioId;
    private String destinatarioTipo;
    private String destinatarioNome;

    private String despacho;
    private String assunto;
    private String observacoes;
    private String motivoTramitacao;

    @Builder.Default
    private boolean urgente = false;

    @Indexed
    private StatusTramitacao status;

    @Builder.Default
    private LocalDateTime dataEnvio = LocalDateTime.now();
    private LocalDateTime dataRecebimento;
    private LocalDateTime dataConclusao;

    private LocalDate prazo;

    private ControleVisibilidade controleVisibilidade;

    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();
    private LocalDateTime dataAtualizacao;
}