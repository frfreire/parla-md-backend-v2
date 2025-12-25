package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.CanalNotificacao;
import br.gov.md.parla_md_backend.domain.enums.PrioridadeNotificacao;
import br.gov.md.parla_md_backend.domain.enums.StatusNotificacao;
import br.gov.md.parla_md_backend.domain.enums.TipoNotificacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notificacoes")
@CompoundIndex(name = "destinatario_status_idx", def = "{'destinatarioId': 1, 'status': 1}")
@CompoundIndex(name = "destinatario_tipo_idx", def = "{'destinatarioId': 1, 'tipo': 1}")
public class Notificacao {

    @Id
    private String id;

    @Indexed
    private TipoNotificacao tipo;

    private PrioridadeNotificacao prioridade;

    @Indexed
    private StatusNotificacao status;

    @Indexed
    private String destinatarioId;
    private String destinatarioNome;
    private String destinatarioEmail;
    private String destinatarioTelefone;

    private String remetenteId;
    private String remetenteNome;

    private String titulo;
    private String mensagem;
    private String mensagemDetalhada;

    private String entidadeRelacionadaTipo; // "PROCESSO", "TRAMITACAO", "PARECER"
    private String entidadeRelacionadaId;

    private String urlAcao;
    private String textoAcao; // "Visualizar Processo", "Responder Tramitação"

    @Builder.Default
    private Map<String, Object> dadosAdicionais = new HashMap<>();

    @Builder.Default
    private List<CanalNotificacao> canaisEnvio = new ArrayList<>();

    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();
    private LocalDateTime dataEnvio;
    private LocalDateTime dataLeitura;

    @Builder.Default
    private int tentativasEnvio = 0;
    private String erroEnvio;

    @Builder.Default
    private boolean agendada = false;
    private LocalDateTime dataAgendamento;

    private LocalDateTime dataExpiracao;

    private String grupoNotificacao;
    @Builder.Default
    private boolean notificacaoAgrupada = false;

    public void marcarComoEnviada() {
        this.status = StatusNotificacao.ENVIADA;
        this.dataEnvio = LocalDateTime.now();
    }

    public void marcarComoLida() {
        this.status = StatusNotificacao.LIDA;
        this.dataLeitura = LocalDateTime.now();
    }

    public void marcarComoErro(String mensagemErro) {
        this.status = StatusNotificacao.ERRO;
        this.erroEnvio = mensagemErro;
    }

    public boolean isLida() {
        return status == StatusNotificacao.LIDA;
    }

    public boolean isExpirada() {
        if (dataExpiracao == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(dataExpiracao);
    }

    public boolean isProntaParaEnvio() {
        if (!agendada) {
            return true;
        }
        if (dataAgendamento == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(dataAgendamento) ||
                LocalDateTime.now().isEqual(dataAgendamento);
    }
}