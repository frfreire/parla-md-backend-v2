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

    @Builder.Default
    private List<String> proposicaoIds = new ArrayList<>();

    @Builder.Default
    private List<String> materiaIds = new ArrayList<>();

    @Builder.Default
    private List<AreaImpacto> areasImpacto = new ArrayList<>();

    private String temaPrincipal;

    private String setorResponsavelId;
    private String setorResponsavelNome;

    private String gestorId;
    private String gestorNome;

    private String analistaResponsavel;

    @Builder.Default
    private boolean requerAnaliseJuridica = false;

    @Builder.Default
    private boolean requerAnaliseOrcamentaria = false;

    @Builder.Default
    private boolean requerConsultaExterna = false;

    @Builder.Default
    private int numeroPareceresPendentes = 0;

    @Builder.Default
    private int numeroPosicionamentosPendentes = 0;

    private String observacoes;

    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();

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