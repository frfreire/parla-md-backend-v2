package br.gov.md.parla_md_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "interacoes_llama")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteracaoLlama {

    @Id
    private String id;

    @Indexed
    private String modelo;

    private String promptUsuario;

    private String promptSistema;

    private String respostaConteudo;

    private Boolean respostaJson;

    private Boolean sucesso;

    private String mensagemErro;

    @Indexed
    private LocalDateTime dataHoraRequisicao;

    private Long duracaoTotalMs;

    private Long duracaoCarregamentoMs;

    private Integer tokensPrompt;

    private Integer tokensResposta;

    private Double temperature;

    private String usuarioId;

    private String contexto;

    @Indexed
    private LocalDateTime dataExpiracao;
}