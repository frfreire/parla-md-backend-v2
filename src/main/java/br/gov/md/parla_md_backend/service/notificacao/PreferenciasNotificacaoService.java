package br.gov.md.parla_md_backend.service.notificacao;

import br.gov.md.parla_md_backend.domain.enums.CanalNotificacao;
import br.gov.md.parla_md_backend.domain.notificacao.PreferenciasNotificacao;
import br.gov.md.parla_md_backend.repository.IPreferenciasNotificacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreferenciasNotificacaoService {

    private final IPreferenciasNotificacaoRepository preferenciasRepository;

    public PreferenciasNotificacao buscarPorUsuario(String usuarioId) {
        return preferenciasRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> criarPreferenciasPadrao(usuarioId));
    }

    @Transactional
    public PreferenciasNotificacao atualizar(PreferenciasNotificacao preferencias) {
        log.info("Atualizando preferências de notificação do usuário: {}",
                preferencias.getUsuarioId());
        return preferenciasRepository.save(preferencias);
    }

    @Transactional
    public void deletarPorUsuario(String usuarioId) {
        preferenciasRepository.deleteByUsuarioId(usuarioId);
        log.info("Preferências do usuário {} removidas", usuarioId);
    }

    private PreferenciasNotificacao criarPreferenciasPadrao(String usuarioId) {
        PreferenciasNotificacao preferencias = PreferenciasNotificacao.builder()
                .usuarioId(usuarioId)
                .canaisHabilitados(List.of(CanalNotificacao.EMAIL, CanalNotificacao.SISTEMA))
                .build();

        return preferenciasRepository.save(preferencias);
    }
}