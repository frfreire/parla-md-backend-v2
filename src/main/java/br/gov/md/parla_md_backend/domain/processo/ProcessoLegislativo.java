package br.gov.md.parla_md_backend.domain.processo;

import br.gov.md.parla_md_backend.domain.legislativo.ItemLegislativo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Processo legislativo que pode agrupar Proposições da Câmara
 * e/ou Matérias do Senado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "processos_legislativos")
public class ProcessoLegislativo {

    @Id
    private String id;

    private String numero;

    private String titulo;

    private String descricao;

    private String temaPrincipal;

    private PrioridadeProcesso prioridade;

    private StatusProcesso status;

    @DBRef
    private List<ItemLegislativo> itensLegislativosVinculados = new ArrayList<>();

    private String setorResponsavel;

    private String analistaResponsavel;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataUltimaAtualizacao;

    private LocalDateTime prazoFinal;

    private String observacoes;

    private List<String> areasImpacto = new ArrayList<>();

    private boolean requerAnaliseJuridica;

    private boolean requerAnaliseOrcamentaria;

    private boolean requerConsultaExterna;

    private Integer numeroPareceresPendentes;

    private Integer numeroPosicionamentosPendentes;

    private String posicaoFinalMD;

    private String justificativaPosicaoFinal;

    private LocalDateTime dataFinalizacao;
}