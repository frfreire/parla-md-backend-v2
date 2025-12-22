package br.gov.md.parla_md_backend.domain.notificacao;

import br.gov.md.parla_md_backend.domain.enums.CanalNotificacao;
import br.gov.md.parla_md_backend.domain.enums.PrioridadeNotificacao;
import br.gov.md.parla_md_backend.domain.enums.TipoNotificacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "preferencias_notificacao")
public class PreferenciasNotificacao {

    @Id
    private String id;

    @Indexed(unique = true)
    private String usuarioId;

    @Builder.Default
    private Map<TipoNotificacao, List<CanalNotificacao>> canaisPorTipo = new HashMap<>();

    @Builder.Default
    private List<CanalNotificacao> canaisHabilitados = new ArrayList<>();

    @Builder.Default
    private boolean respeitarHorarioComercial = false;

    @Builder.Default
    private LocalTime horarioInicioNotificacao = LocalTime.of(8, 0);

    @Builder.Default
    private LocalTime horarioFimNotificacao = LocalTime.of(18, 0);

    @Builder.Default
    private List<Integer> diasSemanaHabilitados = Arrays.asList(1, 2, 3, 4, 5);

    @Builder.Default
    private boolean agruparNotificacoes = true;

    @Builder.Default
    private int intervaloAgrupamentoMinutos = 30;

    @Builder.Default
    private Set<TipoNotificacao> tiposDesabilitados = new HashSet<>();

    @Builder.Default
    private PrioridadeNotificacao prioridadeMinima = PrioridadeNotificacao.BAIXA;

    private String emailAlternativo;
    private String telefoneAlternativo;

    @Builder.Default
    private boolean modoNaoPerturbe = false;
    private LocalTime inicioNaoPerturbe;
    private LocalTime fimNaoPerturbe;

    public boolean deveNotificarAgora() {
        if (modoNaoPerturbe && estaDentroHorarioNaoPerturbe()) {
            return false;
        }

        if (respeitarHorarioComercial && !estaDentroHorarioComercial()) {
            return false;
        }

        int diaSemanaAtual = LocalDateTime.now().getDayOfWeek().getValue();
        return diasSemanaHabilitados.contains(diaSemanaAtual);
    }

    public boolean isTipoDesabilitado(TipoNotificacao tipo) {
        return tiposDesabilitados.contains(tipo);
    }

    public boolean atendePrioridade(PrioridadeNotificacao prioridade) {
        return prioridade.isMaiorQue(prioridadeMinima) || prioridade == prioridadeMinima;
    }

    private boolean estaDentroHorarioComercial() {
        LocalTime agora = LocalTime.now();
        return !agora.isBefore(horarioInicioNotificacao) &&
                !agora.isAfter(horarioFimNotificacao);
    }

    private boolean estaDentroHorarioNaoPerturbe() {
        if (inicioNaoPerturbe == null || fimNaoPerturbe == null) {
            return false;
        }
        LocalTime agora = LocalTime.now();
        return !agora.isBefore(inicioNaoPerturbe) &&
                !agora.isAfter(fimNaoPerturbe);
    }
}