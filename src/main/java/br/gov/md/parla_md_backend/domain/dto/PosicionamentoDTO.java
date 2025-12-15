package br.gov.md.parla_md_backend.domain.dto;

import br.gov.md.parla_md_backend.domain.TipoPosicionamento;
import br.gov.md.parla_md_backend.domain.StatusPosicionamento;
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
public class PosicionamentoDTO {

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
    private List<String> fundamentacao;
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataRecebimento;
    private LocalDateTime prazo;
    private boolean atendidoPrazo;
    private StatusPosicionamento status;
    private String numeroOficio;
    private List<String> anexos;
    private String observacoes;
}