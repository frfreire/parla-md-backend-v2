package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.processo.PrioridadeProcesso;
import br.gov.md.parla_md_backend.domain.enums.StatusProcesso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessoLegislativoDTO {

    private String id;
    private String numero;
    private String titulo;
    private String descricao;
    private String temaPrincipal;
    private PrioridadeProcesso prioridade;
    private StatusProcesso status;
    private List<String> proposicaoIds;
    private String setorResponsavel;
    private String analistaResponsavel;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaAtualizacao;
    private LocalDateTime prazoFinal;
    private List<String> areasImpacto;
    private boolean requerAnaliseJuridica;
    private boolean requerAnaliseOrcamentaria;
    private boolean requerConsultaExterna;
    private Integer numeroPareceresPendentes;
    private Integer numeroPosicionamentosPendentes;
    private String posicaoFinalMD;
    private String justificativaPosicaoFinal;
    private LocalDateTime dataFinalizacao;
    private String observacoes;
}
