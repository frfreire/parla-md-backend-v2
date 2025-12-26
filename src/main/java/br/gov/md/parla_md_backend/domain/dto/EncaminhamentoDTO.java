package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.TipoTramitacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncaminhamentoDTO {

    @NotBlank(message = "ID do processo é obrigatório")
    private String processoId;

    @NotNull(message = "Tipo de tramitação é obrigatório")
    private TipoTramitacao tipo;

    @NotBlank(message = "Remetente é obrigatório")
    private String remetenteId;

    @NotBlank(message = "Tipo do remetente é obrigatório")
    private String remetenteTipo;

    @NotBlank(message = "Nome do remetente é obrigatório")
    private String remetenteNome;

    @NotBlank(message = "Destinatário é obrigatório")
    private String destinatarioId;

    @NotBlank(message = "Tipo do destinatário é obrigatório")
    private String destinatarioTipo;

    @NotBlank(message = "Nome do destinatário é obrigatório")
    private String destinatarioNome;

    @NotBlank(message = "Despacho é obrigatório")
    private String despacho;

    private String assunto;

    private LocalDateTime prazo;

    private boolean urgente;

    private String motivoTramitacao;

    private String observacoes;

}