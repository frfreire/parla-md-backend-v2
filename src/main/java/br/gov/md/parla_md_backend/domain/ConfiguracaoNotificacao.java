package br.gov.md.parla_md_backend.domain;

import br.gov.md.parla_md_backend.domain.enums.TipoNotificacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "configuracao_notificacao")
public class ConfiguracaoNotificacao {

    @Id
    private String id;

    private String emailRemetente;
    private String nomeRemetente;
    private String servidorSmtp;
    private int portaSmtp;
    private boolean smtpAutenticacao;
    private boolean smtpTls;
    private String smtpUsuario;
    private String smtpSenha;

    @Builder.Default
    private boolean pushHabilitado = true;
    private String firebaseServerKey;

    @Builder.Default
    private boolean emailHabilitado = true;

    @Builder.Default
    private boolean sistemaHabilitado = true;

    @Builder.Default
    private boolean whatsappHabilitado = false;
    private String whatsappApiUrl;
    private String whatsappApiToken;

    @Builder.Default
    private int maxTentativasEnvio = 3;

    @Builder.Default
    private int intervaloReenvioMinutos = 5;

    @Builder.Default
    private int maxNotificacoesPorHora = 100;

    @Builder.Default
    private Map<TipoNotificacao, String> templatesEmail = new HashMap<>();

    public boolean isConfiguracaoValida() {
        if (emailHabilitado) {
            if (emailRemetente == null || emailRemetente.isBlank()) {
                return false;
            }
            if (servidorSmtp == null || servidorSmtp.isBlank()) {
                return false;
            }
        }

        if (pushHabilitado) {
            if (firebaseServerKey == null || firebaseServerKey.isBlank()) {
                return false;
            }
        }

        return true;
    }
}