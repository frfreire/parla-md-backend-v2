package br.gov.md.parla_md_backend.service.notificacao.canal;

import br.gov.md.parla_md_backend.domain.notificacao.ConfiguracaoNotificacao;
import br.gov.md.parla_md_backend.domain.notificacao.Notificacao;
import br.gov.md.parla_md_backend.repository.IConfiguracaoNotificacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final IConfiguracaoNotificacaoRepository configuracaoRepository;

    public void enviar(Notificacao notificacao) {
        log.info("Enviando e-mail para: {}", notificacao.getDestinatarioEmail());

        ConfiguracaoNotificacao config = obterConfiguracao();

        if (!config.isEmailHabilitado()) {
            log.warn("Envio de e-mail está desabilitado na configuração");
            throw new IllegalStateException("E-mail desabilitado");
        }

        try {
            String conteudo = construirConteudoEmail(notificacao);

            enviarEmailSimples(
                    notificacao.getDestinatarioEmail(),
                    config.getEmailRemetente(),
                    notificacao.getTitulo(),
                    conteudo
            );

            log.info("E-mail enviado com sucesso para: {}", notificacao.getDestinatarioEmail());

        } catch (Exception e) {
            log.error("Erro ao enviar e-mail para: {}", notificacao.getDestinatarioEmail(), e);
            throw new RuntimeException("Erro ao enviar e-mail", e);
        }
    }

    private void enviarEmailSimples(String para, String de, String assunto, String corpo) {
        log.info("Simulando envio de e-mail:");
        log.info("  De: {}", de);
        log.info("  Para: {}", para);
        log.info("  Assunto: {}", assunto);
        log.info("  Corpo: {} caracteres", corpo.length());
    }

    private String construirConteudoEmail(Notificacao notificacao) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; }");
        html.append(".header { background-color: #0066CC; color: white; padding: 20px; }");
        html.append(".content { padding: 20px; }");
        html.append(".prioridade { display: inline-block; padding: 5px 10px; border-radius: 3px; }");
        html.append(".prioridade-URGENTE { background-color: #dc3545; color: white; }");
        html.append(".prioridade-ALTA { background-color: #ffc107; color: #333; }");
        html.append(".prioridade-NORMAL { background-color: #28a745; color: white; }");
        html.append(".action-button { background-color: #0066CC; color: white; padding: 12px 24px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        html.append("<div class='header'>");
        html.append("<h2>Sistema Parla-MD</h2>");
        html.append("</div>");

        html.append("<div class='content'>");
        html.append("<span class='prioridade prioridade-").append(notificacao.getPrioridade()).append("'>");
        html.append(notificacao.getPrioridade());
        html.append("</span>");

        html.append("<h3>").append(notificacao.getTitulo()).append("</h3>");
        html.append("<p>Olá, <strong>").append(notificacao.getDestinatarioNome()).append("</strong></p>");
        html.append("<p>").append(notificacao.getMensagem()).append("</p>");

        if (notificacao.getMensagemDetalhada() != null) {
            html.append("<div style='padding: 15px; background: #f9f9f9; border-left: 4px solid #0066CC;'>");
            html.append("<p>").append(notificacao.getMensagemDetalhada()).append("</p>");
            html.append("</div>");
        }

        if (notificacao.getUrlAcao() != null) {
            html.append("<p>");
            html.append("<a href='").append(notificacao.getUrlAcao()).append("' class='action-button'>");
            html.append(notificacao.getTextoAcao() != null ? notificacao.getTextoAcao() : "Acessar Sistema");
            html.append("</a>");
            html.append("</p>");
        }

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private ConfiguracaoNotificacao obterConfiguracao() {
        return configuracaoRepository.findAll().stream()
                .findFirst()
                .orElseGet(this::criarConfiguracaoPadrao);
    }

    private ConfiguracaoNotificacao criarConfiguracaoPadrao() {
        ConfiguracaoNotificacao config = ConfiguracaoNotificacao.builder()
                .emailRemetente("noreply@md.gov.br")
                .nomeRemetente("Sistema Parla-MD")
                .emailHabilitado(true)
                .pushHabilitado(true)
                .sistemaHabilitado(true)
                .maxTentativasEnvio(3)
                .intervaloReenvioMinutos(5)
                .maxNotificacoesPorHora(100)
                .build();

        return configuracaoRepository.save(config);
    }
}