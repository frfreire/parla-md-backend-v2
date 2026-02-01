package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.AreaImpacto;
import br.gov.md.parla_md_backend.domain.enums.PrioridadeProcesso;
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
    private List<String> materiaIds;
    private String setorResponsavelId;
    private String setorResponsavelNome;
    private String gestorId;
    private String gestorNome;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaAtualizacao;
    private LocalDateTime prazoFinal;
    private List<AreaImpacto> areasImpacto;
    private boolean requerAnaliseJuridica;
    private boolean requerAnaliseOrcamentaria;
    private boolean requerConsultaExterna;
    private Integer numeroPareceresPendentes;
    private Integer numeroPosicionamentosPendentes;
    private String posicaoFinalMD;
    private String justificativaPosicaoFinal;
    private LocalDateTime dataFinalizacao;
    private String observacoes;

    public ProcessoLegislativoDTO(String id, String numero, String titulo, String descricao,
                                  StatusProcesso status, PrioridadeProcesso prioridade,
                                  List<String> proposicaoIds, List<String> materiaIds,
                                  String temaPrincipal, String setorResponsavelId,
                                  String setorResponsavelNome, String gestorId, String gestorNome,
                                  int numeroPareceresPendentes, int numeroPosicionamentosPendentes,
                                  LocalDateTime dataCriacao, LocalDateTime dataAtualizacao,
                                  LocalDateTime dataConclusao) {
        this.id = id;
        this.numero = numero;
        this.titulo = titulo;
        this.descricao = descricao;
        this.status = status;
        this.prioridade = prioridade;
        this.proposicaoIds = proposicaoIds;
        this.materiaIds = materiaIds;
        this.temaPrincipal = temaPrincipal;
        this.setorResponsavelId = setorResponsavelId;
        this.setorResponsavelNome = setorResponsavelNome;
        this.gestorId = gestorId;
        this.gestorNome = gestorNome;
        this.numeroPareceresPendentes = numeroPareceresPendentes;
        this.numeroPosicionamentosPendentes = numeroPosicionamentosPendentes;
        this.dataCriacao = dataCriacao;
        this.dataUltimaAtualizacao = dataAtualizacao;
        this.dataFinalizacao = dataConclusao;
    }
}