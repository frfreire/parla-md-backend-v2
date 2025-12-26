package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.enums.CanalNotificacao;
import br.gov.md.parla_md_backend.domain.enums.StatusNotificacao;
import br.gov.md.parla_md_backend.domain.Notificacao;

import br.gov.md.parla_md_backend.repository.INotificacaoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvioNotificacaoService {

    private final INotificacaoRepository notificacaoRepository;
    private final EmailService emailService;
    private final PushService pushService;

    @Async("notificacaoExecutor")
    public void enviarNotificacao(Notificacao notificacao) {
        log.info("Enviando notificação {} pelos canais: {}",
                notificacao.getId(), notificacao.getCanaisEnvio());

        boolean sucessoTotal = true;
        StringBuilder erros = new StringBuilder();

        for (CanalNotificacao canal : notificacao.getCanaisEnvio()) {
            try {
                enviarPorCanal(notificacao, canal);
                log.info("Notificação {} enviada com sucesso pelo canal {}",
                        notificacao.getId(), canal);
            } catch (Exception e) {
                sucessoTotal = false;
                String erro = String.format("Erro no canal %s: %s", canal, e.getMessage());
                erros.append(erro).append("; ");
                log.error("Erro ao enviar notificação {} pelo canal {}",
                        notificacao.getId(), canal, e);
            }
        }

        atualizarStatusNotificacao(notificacao, sucessoTotal, erros.toString());
    }

    private void enviarPorCanal(Notificacao notificacao, CanalNotificacao canal) {
        switch (canal) {
            case EMAIL -> emailService.enviar(notificacao);
            case PUSH -> pushService.enviar(notificacao);
            case SISTEMA -> log.debug("Notificação no sistema registrada: {}", notificacao.getId());
            case WHATSAPP -> log.warn("Canal WhatsApp ainda não implementado");
        }
    }

    private void atualizarStatusNotificacao(Notificacao notificacao, boolean sucesso, String erros) {
        notificacao.setTentativasEnvio(notificacao.getTentativasEnvio() + 1);
        notificacao.setDataEnvio(LocalDateTime.now());

        if (sucesso) {
            notificacao.setStatus(StatusNotificacao.ENVIADA);
            notificacao.setErroEnvio(null);
        } else {
            notificacao.setStatus(StatusNotificacao.ERRO);
            notificacao.setErroEnvio(erros);
        }

        notificacaoRepository.save(notificacao);
    }
}