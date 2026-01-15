package br.gov.md.parla_md_backend.service;

import br.gov.md.parla_md_backend.domain.Proposicao;
import br.gov.md.parla_md_backend.domain.enums.StatusTriagem;
import br.gov.md.parla_md_backend.exception.DominioException;
import br.gov.md.parla_md_backend.exception.EntidadeNotFoundException;
import br.gov.md.parla_md_backend.exception.ValidacaoException;
import br.gov.md.parla_md_backend.messaging.RabbitMQProducer;
import br.gov.md.parla_md_backend.repository.IProposicaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProposicaoService {

    private static final String EXCHANGE_PROPOSICAO = "proposicao.exchange";
    private static final String ROTA_NOVA_PROPOSICAO = "proposicao.nova";

    private static final Set<String> TIPOS_PROPOSICAO_VALIDOS = Set.of(
            "PL", "PEC", "MPV", "PDL", "PLP", "PFC", "REQ", "PRC", "PDC"
    );

    private final IProposicaoRepository proposicaoRepository;
    private final RabbitMQProducer rabbitMQProducer;
    private final ProcedimentoService procedureService;

    // =========================================================================
    // OPERAÇÕES CRUD
    // =========================================================================

    @Transactional
    public Proposicao salvar(Proposicao proposicao) {
        if (proposicao == null) {
            throw new ValidacaoException("Proposição não pode ser nula");
        }

        validarProposicao(proposicao);
        validarRegrasNegocio(proposicao);

        try {
            Proposicao proposicaoSalva = persistir(proposicao);

            publicarEvento(proposicaoSalva);

            buscarTramitacoesAsync(proposicaoSalva);

            log.info("Proposição salva com sucesso: {}", proposicaoSalva.getId());

            return proposicaoSalva;

        } catch (Exception e) {
            log.error("Erro ao salvar proposição: {}", e.getMessage(), e);
            throw new DominioException("Falha ao salvar proposição: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Proposicao buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidacaoException("ID da proposição é obrigatório");
        }

        return proposicaoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNotFoundException("Proposição", id));
    }

    @Transactional(readOnly = true)
    public List<Proposicao> buscarTodas() {
        return proposicaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Proposicao> buscarTodas(Pageable pageable) {
        return proposicaoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Proposicao> buscarPorTema(String tema) {
        if (tema == null || tema.trim().isEmpty()) {
            throw new ValidacaoException("Tema não pode ser vazio");
        }
        return proposicaoRepository.findByTema(tema);
    }

    @Transactional(readOnly = true)
    public List<Proposicao> buscarPorPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new ValidacaoException("Datas de início e fim são obrigatórias");
        }
        if (inicio.isAfter(fim)) {
            throw new ValidacaoException("Data inicial não pode ser posterior à data final");
        }

        return proposicaoRepository.findByDataApresentacaoBetween(inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<Proposicao> buscarPorAutor(Long autorId) {
        if (autorId == null || autorId == 0) {
            throw new ValidacaoException("ID do autor é obrigatório");
        }
        return proposicaoRepository.findByIdDeputadoAutor(autorId);
    }

    @Transactional(readOnly = true)
    public Page<Proposicao> buscarPorStatusTriagem(
            StatusTriagem status,
            Pageable pageable) {

        if (status == null) {
            throw new ValidacaoException("Status de triagem é obrigatório");
        }

        return proposicaoRepository.findByStatusTriagem(status, pageable);
    }

    @Transactional
    public Proposicao atualizar(String id, Proposicao proposicao) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidacaoException("ID da proposição é obrigatório");
        }

        if (!proposicaoRepository.existsById(id)) {
            throw new EntidadeNotFoundException("Proposição", id);
        }

        proposicao.setId(id);

        return salvar(proposicao);
    }

    @Transactional
    public void excluir(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidacaoException("ID da proposição é obrigatório");
        }

        if (!proposicaoRepository.existsById(id)) {
            throw new EntidadeNotFoundException("Proposição", id);
        }

        proposicaoRepository.deleteById(id);

        log.info("Proposição excluída com sucesso: {}", id);
    }

    @Transactional
    public Proposicao atualizarStatusTriagem(String id, StatusTriagem novoStatus) {
        Proposicao proposicao = buscarPorId(id);

        proposicao.setStatusTriagem(novoStatus);
        proposicao.setDataUltimaAtualizacao(LocalDateTime.now());

        return persistir(proposicao);
    }

    // =========================================================================
    // MÉTODOS DE VALIDAÇÃO
    // =========================================================================

    private void validarProposicao(Proposicao proposicao) {
        if (proposicao.getSiglaTipo() == null || proposicao.getSiglaTipo().isEmpty()) {
            throw new ValidacaoException("Sigla do tipo da proposição é obrigatória");
        }

        if (proposicao.getAno() == null || proposicao.getAno() <= 0) {
            throw new ValidacaoException("Ano da proposição deve ser maior que zero");
        }

        if (proposicao.getNumero() == null || Integer.parseInt(proposicao.getNumero()) <= 0) {
            throw new ValidacaoException("Número da proposição deve ser maior que zero");
        }

        if (proposicao.getEmenta() == null || proposicao.getEmenta().trim().isEmpty()) {
            throw new ValidacaoException("Ementa da proposição é obrigatória");
        }
    }

    private void validarRegrasNegocio(Proposicao proposicao) {
        if (proposicao.getAno() < 1988) {
            throw new ValidacaoException(
                    "Ano da proposição não pode ser anterior à Constituição de 1988");
        }

        if (!isTipoProposicaoValido(proposicao.getSiglaTipo())) {
            throw new ValidacaoException(
                    "Tipo de proposição inválido: " + proposicao.getSiglaTipo());
        }

        if (proposicao.getAno() > LocalDate.now().getYear()) {
            throw new ValidacaoException(
                    "Ano da proposição não pode ser futuro");
        }
    }

    private boolean isTipoProposicaoValido(String siglaTipo) {
        return TIPOS_PROPOSICAO_VALIDOS.contains(siglaTipo.toUpperCase());
    }

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    private Proposicao persistir(Proposicao proposicao) {
        if (proposicao.getStatusTriagem() == null) {
            proposicao.setStatusTriagem(StatusTriagem.NAO_AVALIADO);
        }

        if (proposicao.getDataUltimaAtualizacao() == null) {
            proposicao.setDataUltimaAtualizacao(LocalDateTime.now());
        }

        return proposicaoRepository.save(proposicao);
    }

    private void publicarEvento(Proposicao proposicao) {
        try {
            rabbitMQProducer.sendMessage(
                    EXCHANGE_PROPOSICAO,
                    ROTA_NOVA_PROPOSICAO,
                    proposicao);

            log.debug("Evento publicado: proposicao.nova - ID={}", proposicao.getId());

        } catch (Exception e) {
            log.error("Erro ao publicar evento da proposição: {}", e.getMessage());
        }
    }

    private void buscarTramitacoesAsync(Proposicao proposicao) {
        try {
            procedureService.buscarESalvarTramitacoes(proposicao);

        } catch (Exception e) {
            log.error("Erro ao buscar tramitações da proposição {}: {}",
                    proposicao.getId(), e.getMessage());
        }
    }

    // =========================================================================
    // MÉTODOS DE ESTATÍSTICAS
    // =========================================================================

    @Transactional(readOnly = true)
    public long contarTotal() {
        return proposicaoRepository.count();
    }

    @Transactional(readOnly = true)
    public long contarPorStatusTriagem(StatusTriagem status) {
        return proposicaoRepository.findByStatusTriagem(status, Pageable.unpaged())
                .getTotalElements();
    }

    @Transactional(readOnly = true)
    public boolean existePorId(String id) {
        return proposicaoRepository.existsById(id);
    }
}