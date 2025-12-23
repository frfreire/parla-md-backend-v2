package br.gov.md.parla_md_backend.service.notificacao.canal;

import br.gov.md.parla_md_backend.domain.notificacao.DispositivoUsuario;
import br.gov.md.parla_md_backend.repository.IDispositivoUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispositivoService {

    private final IDispositivoUsuarioRepository dispositivoRepository;

    @Transactional
    public void registrarDispositivo(String usuarioId, String tokenFcm, String plataforma, String modelo) {
        log.info("Registrando dispositivo para usuário: {}", usuarioId);

        var dispositivoExistente = dispositivoRepository.findByTokenFcm(tokenFcm);

        if (dispositivoExistente.isPresent()) {
            DispositivoUsuario dispositivo = dispositivoExistente.get();
            dispositivo.setUltimoAcesso(LocalDateTime.now());
            dispositivo.setAtivo(true);
            dispositivoRepository.save(dispositivo);
            log.info("Dispositivo atualizado: {}", tokenFcm);
        } else {
            DispositivoUsuario novoDispositivo = DispositivoUsuario.builder()
                    .usuarioId(usuarioId)
                    .tokenFcm(tokenFcm)
                    .plataforma(plataforma)
                    .modelo(modelo)
                    .dataCadastro(LocalDateTime.now())
                    .ultimoAcesso(LocalDateTime.now())
                    .ativo(true)
                    .build();

            dispositivoRepository.save(novoDispositivo);
            log.info("Novo dispositivo registrado: {}", tokenFcm);
        }
    }

    public List<String> obterTokensDispositivo(String usuarioId) {
        return dispositivoRepository.findByUsuarioIdAndAtivoTrue(usuarioId)
                .stream()
                .map(DispositivoUsuario::getTokenFcm)
                .toList();
    }

    @Transactional
    public void removerTokenInvalido(String tokenFcm) {
        log.warn("Removendo token inválido: {}", tokenFcm);

        dispositivoRepository.findByTokenFcm(tokenFcm)
                .ifPresent(dispositivo -> {
                    dispositivo.setAtivo(false);
                    dispositivoRepository.save(dispositivo);
                });
    }

    @Transactional
    public void desativarDispositivo(String usuarioId, String tokenFcm) {
        log.info("Desativando dispositivo: {}", tokenFcm);

        dispositivoRepository.findByUsuarioIdAndTokenFcm(usuarioId, tokenFcm)
                .ifPresent(dispositivo -> {
                    dispositivo.setAtivo(false);
                    dispositivoRepository.save(dispositivo);
                });
    }

    public List<DispositivoUsuario> listarDispositivosUsuario(String usuarioId) {
        return dispositivoRepository.findByUsuarioId(usuarioId);
    }
}
