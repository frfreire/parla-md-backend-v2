package br.gov.md.parla_md_backend.domain.dto;

import jakarta.validation.constraints.Future;
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
public class SolicitacaoPosicionamentoDTO {

    @NotBlank(message = "ID do processo é obrigatório")
    private String processoId;

    @NotBlank(message = "Órgão emissor é obrigatório")
    private String orgaoEmissorId;

    @NotBlank(message = "Nome do órgão emissor é obrigatório")
    private String orgaoEmissorNome;

    @NotBlank(message = "Tipo do órgão é obrigatório")
    private String tipoOrgao;

    @NotBlank(message = "Assunto é obrigatório")
    private String assunto;

    @NotNull(message = "Prazo é obrigatório")
    @Future(message = "Prazo deve ser uma data futura")
    private LocalDateTime prazo;

    private String observacoes;
}
