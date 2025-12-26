package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.StatusTramitacao;
import br.gov.md.parla_md_backend.domain.enums.TipoTramitacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramitacaoDTO {

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
    private String observacoes;

    public TramitacaoDTO(String remetenteId) {
        this.remetenteId = remetenteId;
    }

    public TramitacaoDTO(String id, String processoId, TipoTramitacao tipo, String remetenteId, String remetenteTipo, String remetenteNome, String destinatarioId, String destinatarioTipo, String destinatarioNome, String despacho, String observacoes, StatusTramitacao status, boolean urgente, LocalDateTime dataEnvio, LocalDateTime dataRecebimento, LocalDateTime dataConclusao, LocalDate prazo, LocalDateTime dataCriacao) {
    }
}
