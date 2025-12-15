package br.gov.md.parla_md_backend.domain.parecer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Parecer técnico emitido por setor interno do MD
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pareceres")
public class Parecer {

    @Id
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

    private List<String> fundamentacaoLegal = new ArrayList<>();

    private List<String> impactosIdentificados = new ArrayList<>();

    private String conclusao;

    private LocalDateTime dataSolicitacao;

    private LocalDateTime dataEmissao;

    private LocalDateTime prazo;

    private boolean atendidoPrazo;

    private String aprovadoPorId;

    private String aprovadoPorNome;

    private LocalDateTime dataAprovacao;

    private List<String> anexos = new ArrayList<>();

    private String observacoes;
}