package br.gov.md.parla_md_backend.domain.tramitacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Representa a movimentação de um processo entre setores/órgãos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tramitacoes")
public class Tramitacao {

    @Id
    private String id;

    private String processoId;

    private TipoTramitacao tipo;

    private StatusTramitacao status;

    private String remetenteId;

    private String remetenteTipo;

    private String remetenteNome;

    private String destinatarioId;

    private String destinatarioTipo;

    private String destinatarioNome;

    private String despacho;

    private String assunto;

    private LocalDateTime dataEnvio;

    private LocalDateTime dataRecebimento;

    private LocalDateTime prazo;

    private boolean urgente;

    private String motivoTramitacao;

    private String documentosAnexos;

    private String usuarioRemetenteId;

    private String usuarioDestinatarioId;

    private String observacoes;
}
