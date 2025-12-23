package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.TipoPosicionamento;
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
 * Posicionamento institucional de órgão externo ao MD
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posicionamentos")
public class Posicionamento {

    @Id
    private String id;

    private String processoId;

    private String numero;

    private String orgaoEmissorId;

    private String orgaoEmissorNome;

    private String tipoOrgao;

    private String representanteNome;

    private String representanteCargo;

    private TipoPosicionamento posicao;

    private String assunto;

    private String manifestacao;

    private String justificativa;

    private List<String> fundamentacao = new ArrayList<>();

    private List<String> condicoesRessalvas = new ArrayList<>();

    private String impactoEstimado;

    private LocalDateTime dataSolicitacao;

    private LocalDateTime dataRecebimento;

    private LocalDateTime prazo;

    private boolean atendidoPrazo;

    private StatusPosicionamento status;

    private String documentoOficial;

    private String numeroOficio;

    private List<String> anexos = new ArrayList<>();

    private String observacoes;
}