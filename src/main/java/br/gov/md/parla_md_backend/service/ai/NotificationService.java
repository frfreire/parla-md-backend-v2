package br.gov.md.parla_md_backend.service.ai;

import br.gov.md.parla_md_backend.domain.Materia;
import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.Usuario;
import br.gov.md.parla_md_backend.strategy.NotificacaoDefaultStrategy;
import br.gov.md.parla_md_backend.strategy.NotificacaoMateriaStrategy;
import br.gov.md.parla_md_backend.strategy.NotificacaoProposicaoStrategy;
import br.gov.md.parla_md_backend.strategy.interfaces.NotificacaoStrategy;
import org.springframework.stereotype.Service;
import br.gov.md.parla_md_backend.domain.DocumentoLegislativo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final Map<Class<? extends DocumentoLegislativo>, NotificacaoStrategy> strategies = new HashMap<>();
    private final NotificacaoStrategy defaultStrategy = new NotificacaoDefaultStrategy();

    public NotificationService() {
        strategies.put(Proposicao.class, new NotificacaoProposicaoStrategy());
        strategies.put(Materia.class, new NotificacaoMateriaStrategy());
    }

    public record Notification(String userId, String message, LocalDateTime timestamp) {}

    public List<Notification> generateNotifications(List<DocumentoLegislativo> documents, List<Usuario> usuarios) {
        return documents.stream()
                .flatMap(doc -> usuarios.stream()
                        .filter(usuario -> isRelevantForUser(doc, usuario))
                        .map(usuario -> createNotification(usuario, doc)))
                .toList();
    }

    private boolean isRelevantForUser(DocumentoLegislativo doc, Usuario usuario) {
        return usuario.getInterests().stream()
                .anyMatch(interest -> doc.getEmenta().toLowerCase().contains(interest.toLowerCase()));
    }

    private Notification createNotification(Usuario usuario, DocumentoLegislativo doc) {
        NotificacaoStrategy strategy = strategies.getOrDefault(doc.getClass(), defaultStrategy);
        String message = strategy.createMessage(doc);
        return new Notification(usuario.getId(), message, LocalDateTime.now());
    }
}
