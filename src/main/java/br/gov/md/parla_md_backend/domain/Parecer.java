package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.RecomendacaoParecer;
import br.gov.md.parla_md_backend.domain.enums.TipoParecer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

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

    @Indexed
    private String analista;
    private String analistaNome;

    private TipoParecer tipo;
    private String conteudo;
    private RecomendacaoParecer recomendacao;
    private String justificativa;

    private LocalDateTime dataEmissao;
    private LocalDateTime dataAprovacao;
    private boolean aprovado;

    private String aprovadorId;
    private String aprovadorNome;

    private ControleVisibilidade controleVisibilidade;

    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();
    private LocalDateTime dataAtualizacao;
}