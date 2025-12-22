package br.gov.md.parla_md_backend.domain.notificacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Dispositivo móvel de usuário para notificações push
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "dispositivos_usuario")
@CompoundIndex(name = "usuario_ativo_idx", def = "{'usuarioId': 1, 'ativo': 1}")
public class DispositivoUsuario {

    @Id
    private String id;

    @Indexed
    private String usuarioId;

    @Indexed(unique = true)
    private String tokenFcm;

    private String plataforma;

    private String modelo;

    private String versaoApp;

    @Builder.Default
    private LocalDateTime dataCadastro = LocalDateTime.now();

    private LocalDateTime ultimoAcesso;

    @Builder.Default
    private boolean ativo = true;

    public void atualizarUltimoAcesso() {
        this.ultimoAcesso = LocalDateTime.now();
    }
}