package br.gov.md.parla_md_backend.service.notificacao.canal;

import br.gov.md.parla_md_backend.domain.notificacao.ConfiguracaoNotificacao;
import br.gov.md.parla_md_backend.domain.notificacao.Notificacao;
import br.gov.md.parla_md_backend.repository.IConfiguracaoNotificacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {

    private final IConfiguracaoNotificacaoRepository configuracaoRepository;
    private final DispositivoService dispositivoService;

    public void enviar(Notificacao notificacao) {
        log.info("Enviando notificação push para: {}", notificacao.getDestinatarioId());

        ConfiguracaoNotificacao config = obterConfiguracao();

        if (!config.isPushHabilitado()) {
            log.warn("Envio de push está desabilitado na configuração");
            throw new IllegalStateException("Push desabilitado");
        }

        List<String> tokens = dispositivoService.obterTokensDispositivo(notificacao.getDestinatarioId());

        if (tokens.isEmpty()) {
            log.warn("Usuário {} não possui dispositivos registrados",
                    notificacao.getDestinatarioId());
            throw new IllegalStateException("Nenhum dispositivo registrado");
        }

        try {
            Map<String, String> mensagem = construirMensagem(notificacao);
            enviarParaDispositivos(tokens, mensagem);

            log.info("Notificação push enviada com sucesso para {} dispositivos", tokens.size());

        } catch (Exception e) {
            log.error("Erro ao enviar notificação push", e);
            throw new RuntimeException("Erro ao enviar push", e);
        }
    }

    private Map<String, String> construirMensagem(Notificacao notificacao) {
        Map<String, String> mensagem = new HashMap<>();
        mensagem.put("titulo", notificacao.getTitulo());
        mensagem.put("corpo", notificacao.getMensagem());
        mensagem.put("tipo", notificacao.getTipo().name());
        mensagem.put("notificacaoId", notificacao.getId());

        if (notificacao.getUrlAcao() != null) {
            mensagem.put("urlAcao", notificacao.getUrlAcao());
        }

        if (notificacao.getEntidadeRelacionadaId() != null) {
            mensagem.put("entidadeId", notificacao.getEntidadeRelacionadaId());
            mensagem.put("entidadeTipo", notificacao.getEntidadeRelacionadaTipo());
        }

        return mensagem;
    }

    private void enviarParaDispositivos(List<String> tokens, Map<String, String> mensagem) {
        log.info("Simulando envio de push notification:");//Se o MD quiser, podemos integrar com Firebase Cloud Messaging (FCM) ou outro serviço similar no futuro.
        log.info("  Dispositivos: {}", tokens.size());
        log.info("  Título: {}", mensagem.get("titulo"));
        log.info("  Corpo: {}", mensagem.get("corpo"));

        for (String token : tokens) {
            log.debug("  Token: {}...", token.substring(0, Math.min(20, token.length())));
        }
    }

    private ConfiguracaoNotificacao obterConfiguracao() {
        return configuracaoRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Configuração não encontrada"));
    }
}