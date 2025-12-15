package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.TipoParecer;
import br.gov.md.parla_md_backend.domain.Recomendacao;
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
public class ParecerDTO {

    private String id;
    private String processoId;
    private String numero;
    private String setorEmissorId;
    private String setorEmissorNome;
    private String analistaResponsavelId;
    private String analistaResponsavelNome;
    private TipoParecer tipo;
    private String assunto;
    private String contexto;
    private String analise;
    private Recomendacao recomendacao;
    private String justificativaRecomendacao;
    private List<String> fundamentacaoLegal;
    private List<String> impactosIdentificados;
    private String conclusao;
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataEmissao;
    private LocalDateTime prazo;
    private boolean atendidoPrazo;
    private String aprovadoPorId;
    private String aprovadoPorNome;
    private LocalDateTime dataAprovacao;
    private List<String> anexos;
    private String observacoes;
}
