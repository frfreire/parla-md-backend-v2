package br.gov.md.parla_md_backend.service.tramitacao;

import br.gov.md.parla_md_backend.domain.tramitacao.Tramitacao;
import br.gov.md.parla_md_backend.domain.parecer.Parecer;
import br.gov.md.parla_md_backend.domain.posicionamento.Posicionamento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço para envio de notificações
 * Implementação futura: email, SMS, notificações push
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacaoService {

    public void notificarNovoEncaminhamento(Tramitacao tramitacao) {
        log.info("Notificando novo encaminhamento para: {}",
                tramitacao.getDestinatarioNome());
        // TODO: Implementar envio de notificação
    }

    public void notificarSolicitacaoParecer(Parecer parecer) {
        log.info("Notificando solicitação de parecer para setor: {}",
                parecer.getSetorEmissorNome());
        // TODO: Implementar envio de notificação
    }

    public void notificarParecerEmitido(Parecer parecer) {
        log.info("Notificando emissão de parecer: {}", parecer.getNumero());
        // TODO: Implementar envio de notificação
    }

    public void notificarParecerAprovado(Parecer parecer) {
        log.info("Notificando aprovação de parecer: {}", parecer.getNumero());
        // TODO: Implementar envio de notificação
    }

    public void notificarSolicitacaoPosicionamento(Posicionamento posicionamento) {
        log.info("Notificando solicitação de posicionamento para: {}",
                posicionamento.getOrgaoEmissorNome());
        // TODO: Implementar envio de notificação
    }

    public void notificarPosicionamentoRecebido(Posicionamento posicionamento) {
        log.info("Notificando recebimento de posicionamento: {}",
                posicionamento.getNumero());
        // TODO: Implementar envio de notificação
    }
}