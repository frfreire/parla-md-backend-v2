package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.PrioridadeProcesso;
import br.gov.md.parla_md_backend.domain.enums.StatusProcesso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "processos_legislativos")
public class ProcessoLegislativo {

    @Id
    private String id;

    @Indexed(unique = true)
    private String numero;

    private String titulo;
    private String descricao;

    @Indexed
    private StatusProcesso status;

    @Indexed
    private PrioridadeProcesso prioridade;

    private List<String> proposicaoIds;
    private List<String> materiaIds;

    private List<AreaImpacto> areasImpacto;

    private String temaPrincipal;

    private String setorResponsavelId;
    private String setorResponsavelNome;

    private String gestorId;
    private String gestorNome;

    private String analistaResponsavel;

    // Flags booleanas
    private boolean requerAnaliseJuridica;
    private boolean requerAnaliseOrcamentaria;
    private boolean requerConsultaExterna;

    // Contadores
    private int numeroPareceresPendentes;
    private int numeroPosicionamentosPendentes;

    private String observacoes;

    // Timestamps
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private LocalDateTime dataConclusao;

    public void incrementarPareceresPendentes() {
        this.numeroPareceresPendentes++;
    }

    public void decrementarPareceresPendentes() {
        if (this.numeroPareceresPendentes > 0) {
            this.numeroPareceresPendentes--;
        }
    }

    public void incrementarPosicionamentosPendentes() {
        this.numeroPosicionamentosPendentes++;
    }

    public void decrementarPosicionamentosPendentes() {
        if (this.numeroPosicionamentosPendentes > 0) {
            this.numeroPosicionamentosPendentes--;
        }
    }

    public boolean temPendencias() {
        return numeroPareceresPendentes > 0 || numeroPosicionamentosPendentes > 0;
    }

    public void adicionarProposicao(String proposicaoId) {
        if (proposicaoIds == null) {
            proposicaoIds = new ArrayList<>();
        }
        if (!proposicaoIds.contains(proposicaoId)) {
            proposicaoIds.add(proposicaoId);
        }
    }

    public void adicionarMateria(String materiaId) {
        if (materiaIds == null) {
            materiaIds = new ArrayList<>();
        }
        if (!materiaIds.contains(materiaId)) {
            materiaIds.add(materiaId);
        }
    }

    public void adicionarAreaImpacto(AreaImpacto area) {
        if (areasImpacto == null) {
            areasImpacto = new ArrayList<>();
        }
        areasImpacto.add(area);
    }
}