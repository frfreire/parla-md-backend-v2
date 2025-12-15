package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.TipoParecer;
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
public class SolicitarParecerDTO {

    @NotBlank(message = "ID do processo é obrigatório")
    private String processoId;

    @NotBlank(message = "Setor emissor é obrigatório")
    private String setorEmissorId;

    @NotBlank(message = "Nome do setor emissor é obrigatório")
    private String setorEmissorNome;

    @NotNull(message = "Tipo de parecer é obrigatório")
    private TipoParecer tipo;

    @NotBlank(message = "Assunto é obrigatório")
    private String assunto;

    @NotNull(message = "Prazo é obrigatório")
    @Future(message = "Prazo deve ser uma data futura")
    private LocalDateTime prazo;

    private String observacoes;
}