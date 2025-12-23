package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.enums.PrioridadeProcesso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarProcessoDTO {

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    @NotBlank(message = "Tema principal é obrigatório")
    private String temaPrincipal;

    @NotNull(message = "Prioridade é obrigatória")
    private PrioridadeProcesso prioridade;

    @NotEmpty(message = "Processo deve ter pelo menos uma proposição")
    private List<String> proposicaoIds = new ArrayList<>();

    @NotBlank(message = "Setor responsável é obrigatório")
    private String setorResponsavel;

    private LocalDateTime prazoFinal;

    private List<String> areasImpacto = new ArrayList<>();

    private boolean requerAnaliseJuridica;

    private boolean requerAnaliseOrcamentaria;

    private boolean requerConsultaExterna;

    private String observacoes;
}