package br.gov.md.parla_md_backend.service.notificacao;

import br.gov.md.parla_md_backend.domain.Usuario;
import br.gov.md.parla_md_backend.domain.dto.CriarNotificacaoDTO;
import br.gov.md.parla_md_backend.domain.dto.NotificacaoDTO;
import br.gov.md.parla_md_backend.domain.enums.CanalNotificacao;
import br.gov.md.parla_md_backend.domain.enums.StatusNotificacao;
import br.gov.md.parla_md_backend.domain.notificacao.*;
import br.gov.md.parla_md_backend.exception.RecursoNaoEncontradoException;
import br.gov.md.parla_md_backend.repository.IConfiguracaoNotificacaoRepository;
import br.gov.md.parla_md_backend.repository.INotificacaoRepository;
import br.gov.md.parla_md_backend.repository.IPreferenciasNotificacaoRepository;
import br.gov.md.parla_md_backend.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final INotificacaoRepository notificacaoRepository;
    private final IPreferenciasNotificacaoRepository preferenciasRepository;
    private final IConfiguracaoNotificacaoRepository configuracaoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final EnvioNotificacaoService envioService;

    @Transactional
    public NotificacaoDTO criarNotificacao(CriarNotificacaoDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.destinatarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        PreferenciasNotificacao preferencias = obterPreferencias(dto.destinatarioId());

        if (!deveNotificar(dto, preferencias)) {
            log.debug("Notificação {} ignorada para usuário {} - filtrada por preferências",
                    dto.tipo(), dto.destinatarioId());
            return null;
        }

        List<CanalNotificacao> canais = determinarCanais(dto, preferencias);

        Notificacao notificacao = Notificacao.builder()
                .tipo(dto.tipo())
                .prioridade(dto.prioridade())
                .status(StatusNotificacao.PENDENTE)
                .destinatarioId(usuario.getId())
                .destinatarioNome(usuario.getNome())
                .destinatarioEmail(usuario.getEmail())
                .titulo(dto.titulo())
                .mensagem(dto.mensagem())
                .mensagemDetalhada(dto.mensagemDetalhada())
                .entidadeRelacionadaTipo(dto.entidadeRelacionadaTipo())
                .entidadeRelacionadaId(dto.entidadeRelacionadaId())
                .urlAcao(dto.urlAcao())
                .textoAcao(dto.textoAcao())
                .dadosAdicionais(dto.dadosAdicionais())
                .canaisEnvio(canais)
                .dataCriacao(LocalDateTime.now())
                .agendada(Boolean.TRUE.equals(dto.agendar()))
                .dataAgendamento(dto.dataAgendamento())
                .dataExpiracao(dto.dataExpiracao())
                .build();

        notificacao = notificacaoRepository.save(notificacao);

        if (!notificacao.isAgendada() || notificacao.isProntaParaEnvio()) {
            envioService.enviarNotificacao(notificacao);
        }

        log.info("Notificação {} criada para usuário {}", notificacao.getId(), dto.destinatarioId());
        return converterParaDTO(notificacao);
    }

    public Page<NotificacaoDTO> buscarPorUsuario(String usuarioId, Pageable pageable) {
        return notificacaoRepository.findByDestinatarioIdOrderByDataCriacaoDesc(usuarioId, pageable)
                .map(this::converterParaDTO);
    }

    public List<NotificacaoDTO> buscarNaoLidas(String usuarioId) {
        return notificacaoRepository.findByDestinatarioIdAndStatus(usuarioId, StatusNotificacao.ENVIADA)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public long contarNaoLidas(String usuarioId) {
        return notificacaoRepository.countByDestinatarioIdAndStatus(usuarioId, StatusNotificacao.ENVIADA);
    }

    @Transactional
    public void marcarComoLida(String notificacaoId, String usuarioId) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada"));

        if (!notificacao.getDestinatarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("Notificação não pertence ao usuário");
        }

        notificacao.marcarComoLida();
        notificacaoRepository.save(notificacao);

        log.debug("Notificação {} marcada como lida", notificacaoId);
    }

    @Transactional
    public void marcarTodasComoLidas(String usuarioId) {
        List<Notificacao> naoLidas = notificacaoRepository.findByDestinatarioIdAndStatus(
                usuarioId, StatusNotificacao.ENVIADA);

        naoLidas.forEach(Notificacao::marcarComoLida);
        notificacaoRepository.saveAll(naoLidas);

        log.info("Todas as notificações do usuário {} marcadas como lidas", usuarioId);
    }

    @Transactional
    public void processarNotificacoesAgendadas() {
        List<Notificacao> agendadas = notificacaoRepository
                .findByStatusAndDataAgendamentoBeforeOrderByPrioridadeDesc(
                        StatusNotificacao.PENDENTE,
                        LocalDateTime.now()
                );

        log.info("Processando {} notificações agendadas", agendadas.size());

        agendadas.forEach(notificacao -> {
            if (notificacao.isProntaParaEnvio()) {
                envioService.enviarNotificacao(notificacao);
            }
        });
    }

    @Transactional
    public void reprocessarNotificacoesComErro() {
        ConfiguracaoNotificacao config = obterConfiguracao();

        List<Notificacao> comErro = notificacaoRepository
                .findByStatusAndTentativasEnvioLessThan(
                        StatusNotificacao.ERRO,
                        config.getMaxTentativasEnvio()
                );

        log.info("Reprocessando {} notificações com erro", comErro.size());

        comErro.forEach(envioService::enviarNotificacao);
    }

    @Transactional
    public void limparNotificacoesAntigas(int diasRetencao) {
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(diasRetencao);

        notificacaoRepository.deleteByDataCriacaoBeforeAndStatus(dataLimite, StatusNotificacao.LIDA);

        log.info("Notificações antigas removidas - data limite: {}", dataLimite);
    }

    private boolean deveNotificar(CriarNotificacaoDTO dto, PreferenciasNotificacao preferencias) {
        if (preferencias.isTipoDesabilitado(dto.tipo())) {
            return false;
        }

        if (!preferencias.atendePrioridade(dto.prioridade())) {
            return false;
        }

        if (!preferencias.deveNotificarAgora()) {
            return false;
        }

        return true;
    }

    private List<CanalNotificacao> determinarCanais(CriarNotificacaoDTO dto, PreferenciasNotificacao preferencias) {
        if (dto.canaisEnvio() != null && !dto.canaisEnvio().isEmpty()) {
            return dto.canaisEnvio().stream()
                    .filter(canal -> preferencias.getCanaisHabilitados().contains(canal))
                    .toList();
        }

        List<CanalNotificacao> canais = preferencias.getCanaisPorTipo().get(dto.tipo());
        if (canais != null && !canais.isEmpty()) {
            return new ArrayList<>(canais);
        }

        return new ArrayList<>(preferencias.getCanaisHabilitados());
    }

    private PreferenciasNotificacao obterPreferencias(String usuarioId) {
        return preferenciasRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> criarPreferenciasPadrao(usuarioId));
    }

    private PreferenciasNotificacao criarPreferenciasPadrao(String usuarioId) {
        PreferenciasNotificacao preferencias = PreferenciasNotificacao.builder()
                .usuarioId(usuarioId)
                .canaisHabilitados(List.of(CanalNotificacao.EMAIL, CanalNotificacao.SISTEMA))
                .build();

        return preferenciasRepository.save(preferencias);
    }

    private ConfiguracaoNotificacao obterConfiguracao() {
        return configuracaoRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Configuração de notificação não encontrada"));
    }

    private NotificacaoDTO converterParaDTO(Notificacao notificacao) {
        return new NotificacaoDTO(
                notificacao.getId(),
                notificacao.getTipo(),
                notificacao.getPrioridade(),
                notificacao.getStatus(),
                notificacao.getDestinatarioNome(),
                notificacao.getTitulo(),
                notificacao.getMensagem(),
                notificacao.getUrlAcao(),
                notificacao.getTextoAcao(),
                notificacao.getDadosAdicionais(),
                notificacao.getCanaisEnvio(),
                notificacao.getDataCriacao(),
                notificacao.getDataLeitura(),
                notificacao.isLida()
        );
    }
}
