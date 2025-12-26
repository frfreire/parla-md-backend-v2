package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.RecomendacaoParecer;
import br.gov.md.parla_md_backend.domain.enums.StatusParecer;
import br.gov.md.parla_md_backend.domain.enums.TipoParecer;
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
@Document(collection = "pareceres")
public class Parecer {

    @Id
    private String id;

    @Indexed
    private String numero;

    @Indexed
    private String processoId;

    @Indexed
    private String setorEmissorId;
    private String setorEmissorNome;

    private String analistaResponsavelId;
    private String analistaResponsavelNome;

    private TipoParecer tipo;
    private String assunto;

    private String contexto;
    private String analise;
    private RecomendacaoParecer recomendacao;
    private String justificativaRecomendacao;

    private StatusParecer statusParecer;

    @Builder.Default
    private List<String> fundamentacaoLegal = new ArrayList<>();

    @Builder.Default
    private List<String> impactosIdentificados = new ArrayList<>();

    private String conclusao;

    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataEmissao;
    private LocalDateTime prazo;

    @Builder.Default
    private boolean atendidoPrazo = false;

    private String aprovadoPorId;
    private String aprovadoPorNome;
    private LocalDateTime dataAprovacao;

    @Builder.Default
    private List<String> anexos = new ArrayList<>();

    private String observacoes;

    private ControleVisibilidade controleVisibilidade;

    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();
    private LocalDateTime dataAtualizacao;

    public boolean isPendente() {
        return dataEmissao == null;
    }

    public boolean isEmitido() {
        return dataEmissao != null && dataAprovacao == null;
    }

    public boolean isAprovado() {
        return dataAprovacao != null;
    }

    public boolean isPrazoVencido() {
        if (prazo == null || dataEmissao != null) {
            return false;
        }
        return LocalDateTime.now().isAfter(prazo);
    }
}